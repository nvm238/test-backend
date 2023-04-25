package com.innovattic.medicinfo.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.web.HttpProperties
import com.innovattic.medicinfo.web.security.ConnectWebSocketChannelInterceptor
import com.innovattic.medicinfo.web.security.CookieHandShakeInterceptor
import com.innovattic.medicinfo.web.websocket.StompErrorHandler
import com.innovattic.medicinfo.web.websocket.SubscribeWebSocketChannelInterceptor
import com.innovattic.medicinfo.web.websocket.WebSocketConfig
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker

@Configuration
@EnableWebSocketMessageBroker
open class MessageOrderWebsocketConfig(
    objectMapper: ObjectMapper,
    connectChannelInterceptor: ConnectWebSocketChannelInterceptor,
    subscribeChannelInterceptor: SubscribeWebSocketChannelInterceptor,
    cookieHandShakeInterceptor: CookieHandShakeInterceptor,
    stompErrorHandler: StompErrorHandler,
    httpProperties: HttpProperties,
) : WebSocketConfig(
    objectMapper,
    connectChannelInterceptor,
    subscribeChannelInterceptor,
    cookieHandShakeInterceptor,
    stompErrorHandler,
    httpProperties
) {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        super.configureMessageBroker(registry)

        /**
         * Make sure events are send out strictly in order for unit tests
         * See: https://innovattic.atlassian.net/browse/MEDSLA-166
         */
        registry.setPreservePublishOrder(true)
    }
}
