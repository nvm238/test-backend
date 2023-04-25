package com.innovattic.medicinfo.web.websocket

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.ConversationService
import com.innovattic.medicinfo.logic.ConversationService.Companion.CONVERSATION_TOPIC_ROUTE
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*

@Component
class SubscribeWebSocketChannelInterceptor(
    private val conversationService: ConversationService
) : ChannelInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {

        val accessor = StompHeaderAccessor.wrap(message)

        if (accessor.messageType == SimpMessageType.SUBSCRIBE) {
            val user = accessor.user ?: throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Invalid authorization" }

            val destination = accessor.getFirstNativeHeader("destination")
                ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Invalid destination" }

            if (destination.startsWith(CONVERSATION_TOPIC_ROUTE)) {
                val userPojo = (user as Authentication?)?.details as User?
                    ?: throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Invalid authorization" }

                val conversationId = destination.substring(CONVERSATION_TOPIC_ROUTE.length).let { UUID.fromString(it) }
                val canSubscribe = conversationService.canSubscribe(userPojo, conversationId)
                if (!canSubscribe) {
                    log.info("Conversation with given id not found")
                    return null
                }
            }
        }

        return message
    }
}
