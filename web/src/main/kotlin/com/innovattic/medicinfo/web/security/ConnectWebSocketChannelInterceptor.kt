package com.innovattic.medicinfo.web.security

import com.innovattic.common.error.createResponseStatusException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class ConnectWebSocketChannelInterceptor(
    private val authenticationService: AuthenticationService,
    private val appAuthenticationProvider: AppAuthenticationProvider
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (accessor.command == StompCommand.CONNECT) {

            val webToken = accessor.sessionAttributes?.get("token") as String?

            val token =
                webToken ?: accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION)?.substring(7)
                ?: throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Invalid authorization" }

            val jwtAuthentication =
                appAuthenticationProvider.authenticate(JwtAuthentication(authenticationService.verifyJwt(token)))

            accessor.user = jwtAuthentication
        }
        return message
    }
}
