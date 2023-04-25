package com.innovattic.medicinfo.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.ConversationService
import com.innovattic.medicinfo.logic.HeartBeatService
import com.innovattic.medicinfo.logic.OnlineEmployeeService
import com.innovattic.medicinfo.logic.dto.HeartBeatDto
import com.innovattic.medicinfo.logic.dto.OnlineEmployeeDto
import com.innovattic.medicinfo.logic.dto.conversation.ConversationEvent
import com.innovattic.medicinfo.web.security.AuthenticationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendEmptyMsg
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.builtin.builtIn
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.messaging.simp.user.SimpUserRegistry
import java.util.*

abstract class BaseWebSocketTest : BaseEndpointTest() {
    @Autowired lateinit var heartBeatService: HeartBeatService
    @Autowired lateinit var onlineEmployeeService: OnlineEmployeeService
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var simpUserRegistry: SimpUserRegistry
    private lateinit var url: String

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @BeforeAll
    fun configureUrl(@LocalServerPort port: Int) {
        url = "ws://localhost:$port/api/v1/chat"
    }

    private val stompClient by lazy {
        // Note that the client used for tests is currently *not* using anything from Spring
        StompClient(WebSocketClient.builtIn())
    }

    suspend fun StompSession.sendStartTyping(conversationId: UUID) = sendEmptyMsg("/v1/app/conversation/$conversationId/typing/start")
    suspend fun StompSession.sendStopTyping(conversationId: UUID) = sendEmptyMsg("/v1/app/conversation/$conversationId/typing/stop")
    suspend fun StompSession.subscribeConversation(id: UUID) =
        subscribeAndWait<ConversationEvent>(ConversationService.CONVERSATION_TOPIC_ROUTE + id)
    suspend fun StompSession.subscribeHeartbeat() = subscribeAndWait<HeartBeatDto>(HeartBeatService.HEARTBEAT_TOPIC_ROUTE)
    suspend fun StompSession.subscribeOnline() = subscribeAndWait<OnlineEmployeeDto>(OnlineEmployeeService.TOPIC_ROUTE)

    private fun getSubscriptionCount(topic: String): Int {
        return simpUserRegistry.findSubscriptions { it.destination == topic }.count()
    }

    /**
     * The subscribe operation is async, so we risk running the testcase before the subscription has been registered
     * by spring's internals. Unfortunately Spring's subscription handler (in SimpUserRegistry) is placed as *last*
     * handler in the chain, so we can't just hook in a listener.
     * This simply uses a lookup on the registry to wait until a new subscriber is seen.
     * (see: https://blog.jcore.com/2017/11/sleepless-integration-testing-of-spring-stomp-over-websockets/)
     */
    private suspend inline fun <reified T: Any> StompSession.subscribeAndWait(topic: String): Flow<T> {
        val targetNumberOfSubscribers = getSubscriptionCount(topic) + 1
        val originalFlow = subscribeText(topic)

        while(getSubscriptionCount(topic) != targetNumberOfSubscribers) {
            delay(50)
        }

        return originalFlow.map { objectMapper.readValue(it, T::class.java) }
    }

    suspend fun <T> Flow<T>.readSingle() = read(1).single()
    suspend fun <T> Flow<T>.read(count: Int) = withTimeout(15000) {
        onEach { log.debug("Received message: {}", it) }
            .take(count)
            .toCollection(mutableListOf())
    }

    suspend fun createSession(user: User) = stompClient.connect(url,
        customStompConnectHeaders = mapOf(HttpHeaders.AUTHORIZATION to "Bearer ${accessToken(user)}"))

    // TODO doesn't work; when it does, use for some employee connections
    suspend fun createSessionViaCookie(user: User) = stompClient.connect(url,
        customStompConnectHeaders = mapOf(HttpHeaders.COOKIE to "${AuthenticationService.SESSION_COOKIE}=${accessToken(user)}"))
}
