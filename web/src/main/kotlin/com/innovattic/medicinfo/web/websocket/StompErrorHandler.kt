package com.innovattic.medicinfo.web.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.error.ResponseStatusWithCodeException
import com.innovattic.medicinfo.web.dto.StompErrorDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler

@Component
class StompErrorHandler(
    private val objectMapper: ObjectMapper
) : StompSubProtocolErrorHandler() {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun handleClientMessageProcessingError(
        clientMessage: Message<ByteArray>,
        throwable: Throwable
    ): Message<ByteArray>? {
        log.error("Stomp error occurred: ", throwable)

        if (throwable is MessageDeliveryException && throwable.mostSpecificCause is ResponseStatusWithCodeException) {
            return createErrorMessage(throwable.mostSpecificCause as ResponseStatusWithCodeException)
        }

        return super.handleClientMessageProcessingError(clientMessage, throwable)
    }

    private fun createErrorMessage(
        responseStatusWithCodeException: ResponseStatusWithCodeException
    ): Message<ByteArray> {

        val stompError = StompErrorDto(
            status = responseStatusWithCodeException.status,
            errorCode = responseStatusWithCodeException.errorCode
        )
        val errorMessage = objectMapper.writeValueAsString(stompError)
        val accessor = StompHeaderAccessor.create(StompCommand.ERROR)
        accessor.message = errorMessage
        accessor.setLeaveMutable(true)
        return MessageBuilder.createMessage(errorMessage.toByteArray(), accessor.messageHeaders)
    }
}
