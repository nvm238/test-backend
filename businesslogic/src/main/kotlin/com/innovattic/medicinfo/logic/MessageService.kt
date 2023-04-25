package com.innovattic.medicinfo.logic

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.database.databaseUtcToZoned
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.error.failResponseIf
import com.innovattic.common.notification.PushNotificationPlatform
import com.innovattic.common.notification.PushNotificationService
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.MessageDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.ActionDto
import com.innovattic.medicinfo.database.dto.ActionType
import com.innovattic.medicinfo.database.dto.ActionType.Companion.VIDEO_CHAT_MESSAGE_APPOINTMENT_EXTERNAL_ID
import com.innovattic.medicinfo.database.dto.ActionType.Companion.VIDEO_CHAT_MESSAGE_APPOINTMENT_ID
import com.innovattic.medicinfo.database.dto.AttachmentDto
import com.innovattic.medicinfo.database.dto.AttachmentType
import com.innovattic.medicinfo.database.dto.ConversationDto
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.MessageDto
import com.innovattic.medicinfo.database.dto.TranslationCombineUtil
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.Message
import com.innovattic.medicinfo.dbschema.tables.pojos.MessageAttachment
import com.innovattic.medicinfo.dbschema.tables.pojos.MessageView
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.CreateMessageDto
import com.innovattic.medicinfo.logic.dto.conversation.ConversationEvent
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*

@Component
class MessageService(
    private val dao: MessageDao,
    private val conversationService: ConversationService,
    private val pushNotificationService: PushNotificationService,
    private val labelDao: LabelDao,
    private val objectMapper: ObjectMapper,
    private val userDao: UserDao,
    private val salesforceService: SalesforceService,
    private val appointmentService: AppointmentService,
    private val imageService: ImageService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun createWithAttachment(
        user: User,
        conversationPublicId: UUID,
        fileName: String,
        fileType: String,
        data: InputStream,
        text: String? = null,
    ): MessageDto {
        imageService.validateContentType(fileType)
        imageService.checkTimeConstraint(user.publicId)

        val conversation = conversationService.get(conversationPublicId, user)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Conversation with given id not found" }
        throwIfConversationArchivedForCustomer(conversation, user)
        val message = dao.create(user.id, conversation.id, text.orEmpty(), null, null)
        val imageBytes = data.readAllBytes()
        val attachment =
            imageService.createImage(fileName, fileType, imageBytes, conversation.labelId, user.publicId, message.id)
        val messageDto = mapMessage(
            message,
            user,
            conversation.language,
            AttachmentDto(attachmentUrl(conversation.publicId, attachment.publicId), AttachmentType.IMAGE)
        )
        onNewMessage(user, conversation, message, messageDto, attachment, imageBytes)
        return messageDto
    }

    fun create(
        user: User,
        conversationPublicId: UUID,
        dto: CreateMessageDto,
    ): MessageDto {
        failResponseIf(
            dto.action != null && (user.role !in listOf(UserRole.EMPLOYEE, UserRole.ADMIN)),
            HttpStatus.FORBIDDEN
        ) {
            "Need to be user of type ${UserRole.EMPLOYEE} OR ${UserRole.ADMIN} to create messages with actions"
        }

        val conversation = conversationService.get(conversationPublicId, user)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Conversation with given id not found" }
        throwIfConversationArchivedForCustomer(conversation, user)

        val enrichedActionDto = dto.action?.let { enrichActionContext(it) }
        val message = dao.create(user.id, conversation.id, dto.message!!, enrichedActionDto, dto.translatedMessage)
        val messageDto = mapMessage(message, user, conversation.language)

        onNewMessage(user, conversation, message, messageDto)
        return messageDto
    }

    /**
     * Checks if given conversation exists and then updates the message in the conversation with given translation
     *
     * @param user currently authenticated user
     * @param conversationPublicId UUID of the conversation
     * @param messagePublicId UUID of the message in the conversation
     * @param translation String containing translated text
     */
    fun addTranslation(user: User, conversationPublicId: UUID, messagePublicId: UUID, translation: String) {
        val conversation = conversationService.get(conversationPublicId, user)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Conversation with id=$conversationPublicId not found" }
        throwIfConversationArchivedForCustomer(conversation, user)
        val message = dao.getMessageByUUID(messagePublicId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) {
                "Message with id=$messagePublicId not found in conversation with id=$conversationPublicId"
            }
        message.translatedMessage = translation

        dao.updateMessage(message)
    }

    private fun enrichActionContext(dto: ActionDto): ActionDto {
        // Enrich the actionContext with the appointment public_id based on the salesforce appointment id in case of
        // a video chat message as the frontend needs it to link the message with its appointments.
        if (dto.type == ActionType.VIDEO_CHAT_MESSAGE &&
            dto.context?.contains(VIDEO_CHAT_MESSAGE_APPOINTMENT_EXTERNAL_ID) == true
        ) {
            val actionContext = dto.context?.toMutableMap() ?: mutableMapOf()
            val appointmentId = appointmentService.getAppointmentPublicIdBySalesforceAppointmentId(
                actionContext[VIDEO_CHAT_MESSAGE_APPOINTMENT_EXTERNAL_ID].toString()
            )
            actionContext[VIDEO_CHAT_MESSAGE_APPOINTMENT_ID] = appointmentId
            return ActionDto(dto.type, actionContext)
        }

        return dto
    }

    private fun throwIfConversationArchivedForCustomer(conversation: Conversation, user: User) {
        if (conversation.status == ConversationStatus.ARCHIVED && user.role == UserRole.CUSTOMER) {
            throw createResponseStatusException(
                HttpStatus.BAD_REQUEST,
                code = ErrorCodes.CONVERSATION_EXPIRED
            ) { "Conversation with given id expired" }
        }
    }

    private fun onNewMessage(
        user: User,
        conversation: Conversation,
        message: Message,
        messageDto: MessageDto,
        attachment: MessageAttachment? = null,
        imageBytes: ByteArray? = null
    ) {
        if (user.role == UserRole.CUSTOMER) {
            salesforceService.onCustomerMessageAsync(
                labelDao.getById(user.labelId)!!,
                conversation,
                message,
                attachment,
                imageBytes,
                user
            )
        }

        conversationService.sendEvent(conversation.publicId, ConversationEvent(message = messageDto))

        val connectedUsers = conversationService.findConnectedUsers(conversation.publicId)
        connectedUsers.forEach {
            conversationService.received(conversation.publicId, it)
        }

        // trigger push notification if sender is not the customer, and customer is not subscribed via websocket
        if (user.id != conversation.customerId && connectedUsers.none { it.id == conversation.customerId }) {
            val customer = userDao.getById(conversation.customerId)!!
            if (customer.snsEndpointArn != null) {
                val label = labelDao.getById(customer.labelId)!!
                try {
                    val count = dao.count(conversation.id, conversation.readByCustomer)
                    val pushData = mapOf(
                        "conversationId" to conversation.publicId,
                        "count" to count,
                        "archived" to (conversation.status == ConversationStatus.ARCHIVED)
                    )
                    pushNotificationService.sendCombinedNotification(
                        customer.snsEndpointArn,
                        label.name,
                        label.pushNotificationText ?: "Er staat een bericht voor je klaar",
                        pushData,
                        message.publicId.toString(),
                        badge = count,
                        sound = "default",
                        platform = PushNotificationPlatform.GCM,
                    )
                } catch (e: Exception) {
                    // sending push notifications can fail, for example, Amazon SNS will disable the user's endpoint
                    // when google/apple notifies the device token is no longer valid.
                    // be sure not to fail the whole request, which would prevent the message from being saved in the DB
                    // Since this can happen when a user deletes the app or when a token expires and we can't
                    // really take any action on this, we log it at info level.
                    log.info("Could not send push notification for new messages to customer {}", customer.publicId, e)
                }
            }
        }
    }

    fun get(
        user: User,
        conversationPublicId: UUID,
        query: String?,
        order: List<String>?
    ): ConversationDto {
        val conversation = conversationService.get(conversationPublicId, user)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Conversation with given id not found" }

        val customer = userDao.getById(conversation.customerId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Conversation with given id not found" }

        conversationService.received(conversation.publicId, user)

        val messages = dao.get(conversation.id, order, query)
            .map { mapMessage(it, user.role, conversation.language) }
        return conversationService.mapConversation(customer, conversation, messages)
    }

    private fun mapMessage(messageView: MessageView, userRole: UserRole, language: String? = null): MessageDto {
        var message = messageView.message
        if (userRole == UserRole.CUSTOMER && messageView.userRole == UserRole.EMPLOYEE) {
            // Message is requested by a customer (via app) - but older apps don't use the
            // 'translatedMessage' field. To be backwards compatible, just concat the translated message
            // into the main message.
            // Do this only when the message is sent by the employee; we don't want to show
            // translations of customer-sent messages in the app.
            message = TranslationCombineUtil.combine(messageView.message, messageView.translatedMessage, language)
        }

        return MessageDto(
            userId = messageView.userPublicId,
            id = messageView.publicId,
            userName = messageView.userName,
            userRole = messageView.userRole,
            created = messageView.created.toZonedDateTime(),
            message = message,
            translatedMessage = messageView.translatedMessage,
            action = messageView.actionType?.let { actionType ->
                val actionContext = messageView.actionContext?.let { actionContext ->
                    objectMapper.readValue(actionContext, ACTION_MAP_TYPE_REFERENCE)
                }
                ActionDto(actionType, actionContext)
            },
            attachment = messageView.attachmentId?.let {
                AttachmentDto(
                    attachmentUrl(messageView.conversationPublicId, messageView.attachmentId),
                    messageView.attachmentContentType
                )
            },
        )
    }

    private fun attachmentUrl(conversationPublicId: UUID, attachmentId: UUID): String =
        "conversation/$conversationPublicId/image/$attachmentId"

    private fun mapMessage(
        message: Message,
        user: User,
        language: String?,
        attachmentDto: AttachmentDto? = null
    ): MessageDto = MessageDto(
        userId = user.publicId,
        id = message.publicId,
        userName = user.name,
        userRole = user.role,
        created = message.created.toZonedDateTime(),
        message = TranslationCombineUtil.combine(message.message, message.translatedMessage, language),
        translatedMessage = message.translatedMessage,
        action = message.actionType?.let { actionType ->
            val actionContext = message.actionContext?.let { actionContext ->
                objectMapper.readValue(actionContext, ACTION_MAP_TYPE_REFERENCE)
            }
            ActionDto(actionType, actionContext)
        },
        attachment = attachmentDto
    )

    private fun LocalDateTime.toZonedDateTime() = databaseUtcToZoned(this)

    companion object {
        private val ACTION_MAP_TYPE_REFERENCE = object : TypeReference<Map<String, Any>>() {}
    }
}
