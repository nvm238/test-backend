package com.innovattic.medicinfo.logic.listener

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.ConversationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import java.util.UUID

@Component
class WebSocketListener(
    private val conversationService: ConversationService
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun on(event: SessionDisconnectEvent) {
        log.info("SessionDisconnectEvent: ${event.user}")
        val userPojo = (event.user as Authentication?)?.details as User?
            ?: return

        conversationService.handleDisconnect(userPojo)
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun on(event: SessionSubscribeEvent) {
        val userPojo = (event.user as Authentication?)?.details as User?
            ?: return

        val stompMessage = StompHeaderAccessor.wrap(event.message)
        val destination =
            stompMessage.destination ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Invalid destination" }

        if (destination.startsWith(ConversationService.CONVERSATION_TOPIC_ROUTE)) {

            val conversationId = destination.substring(ConversationService.CONVERSATION_TOPIC_ROUTE.length).let { UUID.fromString(it) }
            conversationService.handleSubscribe(userPojo, conversationId)
        }
    }
}
