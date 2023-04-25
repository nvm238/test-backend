package com.innovattic.medicinfo.web.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.web.HttpProperties
import com.innovattic.medicinfo.web.security.ConnectWebSocketChannelInterceptor
import com.innovattic.medicinfo.web.security.CookieHandShakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.DefaultContentTypeResolver
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.util.MimeTypeUtils
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
open class WebSocketConfig(
    private val objectMapper: ObjectMapper,
    private val connectChannelInterceptor: ConnectWebSocketChannelInterceptor,
    private val subscribeChannelInterceptor: SubscribeWebSocketChannelInterceptor,
    private val cookieHandShakeInterceptor: CookieHandShakeInterceptor,
    private val stompErrorHandler: StompErrorHandler,
    private val httpProperties: HttpProperties,
) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/v1/topic")
        registry.setApplicationDestinationPrefixes("/v1/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/v1/chat")
            .setAllowedOrigins(*httpProperties.allowedOrigins.toTypedArray())
            .setAllowedOriginPatterns(*httpProperties.allowedPatterns.toTypedArray())
            .addInterceptors(cookieHandShakeInterceptor)
        registry.setErrorHandler(stompErrorHandler)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(
            connectChannelInterceptor,
            subscribeChannelInterceptor
        )
        super.configureClientInboundChannel(registration)
    }

    override fun configureMessageConverters(messageConverters: MutableList<MessageConverter>): Boolean {
        val resolver = DefaultContentTypeResolver()
        resolver.defaultMimeType = MimeTypeUtils.APPLICATION_JSON

        val converter = MappingJackson2MessageConverter()
        converter.objectMapper = objectMapper
        converter.contentTypeResolver = resolver

        messageConverters.add(converter)
        return super.configureMessageConverters(messageConverters)
    }
}
