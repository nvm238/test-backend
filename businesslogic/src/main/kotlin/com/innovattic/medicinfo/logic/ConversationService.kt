package com.innovattic.medicinfo.logic

import com.innovattic.common.database.databaseUtcToZoned
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dto.ConversationDto
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.CustomerInfoDto
import com.innovattic.medicinfo.database.dto.MessageDto
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.conversation.ConversationEvent
import com.innovattic.medicinfo.logic.dto.conversation.ReadDto
import com.innovattic.medicinfo.logic.dto.conversation.ReceivedDto
import com.innovattic.medicinfo.logic.dto.conversation.StartTypingDto
import com.innovattic.medicinfo.logic.dto.conversation.StopTypingDto
import com.innovattic.medicinfo.logic.dto.conversation.UserConnectedDto
import com.innovattic.medicinfo.logic.dto.conversation.UserDisconnectedDto
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUser
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@Component
class ConversationService(
    @Lazy private val simpMessagingTemplate: SimpMessagingTemplate,
    @Lazy private val simpUserRegistry: SimpUserRegistry,
    private val clock: Clock,
    private val dao: ConversationDao
) {
    @Deprecated("Conversations are to be created through the triage")
    fun create(user: User): ConversationDto {
        val conversation = dao.create(user.id, user.labelId, ConversationStatus.OPEN)
        return mapConversation(user, conversation, emptyList())
    }

    fun startTyping(conversationPublicId: UUID, user: User) {
        val conversation = get(conversationPublicId, user)
            ?: throw throwExceptionWithConversationId(conversationPublicId)
        sendEvent(
            conversation.publicId,
            ConversationEvent(startTyping = StartTypingDto(user.publicId))
        )
    }

    fun handleDisconnect(user: User) {
        val simpUser = findSimpUser(user) ?: return
        val connectedConversationIds = simpUser.sessions.flatMap { simpSession ->
            simpSession.subscriptions.mapNotNull {
                if (it.destination.startsWith(CONVERSATION_TOPIC_ROUTE)) {
                    it.destination.substring(CONVERSATION_TOPIC_ROUTE.length).let { UUID.fromString(it) }
                } else null
            }
        }

        connectedConversationIds.forEach { conversationId ->
            sendEvent(
                conversationId,
                ConversationEvent(userDisconnected = UserDisconnectedDto(user.publicId))
            )
        }
    }

    fun stopTyping(conversationPublicId: UUID, user: User) {
        val conversation = get(conversationPublicId, user)
            ?: throw throwExceptionWithConversationId(conversationPublicId)
        sendEvent(
            conversation.publicId,
            ConversationEvent(stopTyping = StopTypingDto(user.publicId))
        )
    }

    fun archive(conversationPublicId: UUID, user: User) {
        if (user.role == UserRole.ADMIN) {
            val conversation = dao.get(conversationPublicId)
                ?: throw throwExceptionWithConversationId(conversationPublicId)
            dao.archive(
                conversationPublicId = conversationPublicId,
                customerId = conversation.customerId,
                labelId = conversation.labelId
            )
        } else {
            dao.archive(conversationPublicId = conversationPublicId, customerId = user.id, labelId = user.labelId)
        }
    }

    fun received(conversationPublicId: UUID, user: User) {
        val conversation = get(conversationPublicId, user)
            ?: throw throwExceptionWithConversationId(conversationPublicId)
        val isCustomer = user.role == UserRole.CUSTOMER
        dao.received(conversation.id, isCustomer)
        sendEvent(
            conversation.publicId,
            ConversationEvent(received = ReceivedDto(user.publicId, ZonedDateTime.now(clock)))
        )
    }

    fun read(conversationPublicId: UUID, user: User) {
        val conversation = get(conversationPublicId, user)
            ?: throw throwExceptionWithConversationId(conversationPublicId)

        val isCustomer = user.role == UserRole.CUSTOMER
        dao.read(conversation.id, isCustomer)
        sendEvent(
            conversation.publicId,
            ConversationEvent(read = ReadDto(user.publicId, ZonedDateTime.now(clock)))
        )
    }

    fun sendEvent(conversationPublicId: UUID, conversationEvent: ConversationEvent) {
        simpMessagingTemplate.convertAndSend(
            "$CONVERSATION_TOPIC_ROUTE$conversationPublicId",
            conversationEvent
        )
    }

    fun canSubscribe(user: User, conversationPublicId: UUID): Boolean {
        val conversation = this.get(conversationPublicId, user)
        return (conversation != null && conversation.status != ConversationStatus.ARCHIVED)
    }

    fun handleSubscribe(user: User, conversationPublicId: UUID) {
        val conversation = this.get(conversationPublicId, user)
            ?: throw throwExceptionWithConversationId(conversationPublicId)
        val connectedUsers = findConnectedUsers(conversationPublicId)

        sendEvent(
            conversation.publicId,
            ConversationEvent(userConnected = UserConnectedDto(user.publicId))
        )

        connectedUsers.forEach { connectedUser ->
            sendEvent(
                conversation.publicId,
                ConversationEvent(userConnected = UserConnectedDto(connectedUser.publicId))
            )
        }
    }

    fun mapConversation(
        customer: User,
        conversation: Conversation,
        messages: List<MessageDto>
    ): ConversationDto = ConversationDto(
        id = conversation.publicId,
        created = databaseUtcToZoned(conversation.created),
        customer = CustomerInfoDto(
            id = customer.publicId,
            name = customer.name,
            gender = customer.gender,
            age = customer.age
        ),
        deliveredToCustomer = conversation.deliveredToCustomer?.toZonedDateTime(),
        deliveredToEmployee = conversation.deliveredToEmployee?.toZonedDateTime(),
        readByCustomer = conversation.readByCustomer?.toZonedDateTime(),
        readByEmployee = conversation.readByEmployee?.toZonedDateTime(),
        status = conversation.status,
        messages = messages
    )

    fun findConnectedUsers(conversationPublicId: UUID): List<User> {
        val subscriptions = simpUserRegistry.findSubscriptions { subscription ->
            subscription.destination.equals("$CONVERSATION_TOPIC_ROUTE$conversationPublicId")
        }
        return subscriptions.mapNotNull {
            (it.session.user.principal as Authentication?)?.details as User?
        }
    }

    fun findSimpUser(user: User): SimpUser? =
        simpUserRegistry.users.find { ((it.principal as Authentication?)?.details as User?)?.id == user.id }

    fun get(conversationPublicId: UUID, user: User): Conversation? {
        return when (user.role) {
            UserRole.EMPLOYEE -> getAsEmployee(conversationPublicId)
            UserRole.ADMIN -> getAsEmployee(conversationPublicId)
            UserRole.CUSTOMER -> getAsCustomer(conversationPublicId, user)
            else -> null
        }
    }

    private fun getAsCustomer(conversationPublicId: UUID, user: User): Conversation? =
        dao.get(
            conversationPublicId = conversationPublicId,
            customerId = user.id,
            labelId = user.labelId
        )

    private fun getAsEmployee(conversationPublicId: UUID): Conversation? =
        dao.get(conversationPublicId = conversationPublicId)

    private fun LocalDateTime.toZonedDateTime() = databaseUtcToZoned(this)

    private fun throwExceptionWithConversationId(id: UUID): Throwable {
        return createResponseStatusException(HttpStatus.NOT_FOUND) { "Conversation with given id not found, id: $id" }
    }
    companion object {
        const val CONVERSATION_TOPIC_ROUTE = "/v1/topic/conversation/"
    }
}
