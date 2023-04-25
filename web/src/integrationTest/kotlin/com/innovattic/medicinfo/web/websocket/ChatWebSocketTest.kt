package com.innovattic.medicinfo.web.websocket

import com.innovattic.medicinfo.database.dto.ConversationDto
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.CreateMessageDto
import com.innovattic.medicinfo.web.BaseWebSocketTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ChatWebSocketTest : BaseWebSocketTest() {
    @Test
    fun receiveConversation_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val flow = runBlocking { createSession(user).subscribeConversation(conversation.id) }

        messageService.create(user, conversation.id, CreateMessageDto(message = "Hello"))

        val events = runBlocking { flow.drop(1).read(2) }
        with(events[0]) {
            assertNull(read)
            assertNull(received)
            assertEquals("Hello", message?.message)
            assertEquals(user.publicId, message?.userId)
            assertEquals(user.role, message?.userRole)
            assertEquals(user.name, message?.userName)
        }
        with(events[1]) {
            assertNull(read)
            assertNull(message)
            assertEquals(user.publicId, received?.userId)
        }
    }

    @Test
    fun receiveRead_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)
        val flow = runBlocking { createSession(user).subscribeConversation(conversation.id) }

        conversationService.read(conversation.id, employee)

        val event = runBlocking { flow.drop(1).readSingle() }
        with(event) {
            assertNull(message)
            assertNull(received)
            assertEquals(employee.publicId, read?.userId)
        }
    }

    @Test
    fun receiveReceived_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)
        val flow = runBlocking { createSession(user).subscribeConversation(conversation.id) }

        conversationService.received(conversation.id, employee)

        val event = runBlocking { flow.drop(1).readSingle() }
        with(event) {
            assertNull(message)
            assertNull(read)
            assertEquals(employee.publicId, received?.userId)
        }
    }

    @Test
    fun receiveReceived_works_forEmployee() {
        val customerOne = createCustomer(createLabel(), "c1")
        val customerTwo = createCustomer(createLabel(), "c2")
        val employee = createEmployee("wsemployee")
        val conversationOne = conversationService.create(customerOne)
        val conversationTwo = conversationService.create(customerTwo)
        val (session, flowOne) = runBlocking {
            val session = createSession(employee)
            Pair(session, session.subscribeConversation(conversationOne.id))
        }

        conversationService.received(conversationTwo.id, customerTwo)
        conversationService.received(conversationOne.id, customerOne)

        val flowTwo = runBlocking { session.subscribeConversation(conversationTwo.id) }
        conversationService.received(conversationTwo.id, customerTwo)

        runBlocking {
            with(flowOne.drop(1).readSingle()) {
                assertEquals(customerOne.publicId, received?.userId)
            }
            with(flowTwo.drop(1).readSingle()) {
                assertEquals(customerTwo.publicId, received?.userId)
            }
        }
    }

    @Test
    fun startTyping_byEmployee_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)
        val flow = runBlocking { createSession(user).subscribeConversation(conversation.id) }

        conversationService.startTyping(conversation.id, employee)

        val event = runBlocking { flow.drop(1).readSingle() }
        with(event) {
            assertNull(stopTyping)
            assertEquals(employee.publicId, startTyping?.userId)
        }
    }

    @Test
    fun startAndStopTyping_byCustomer_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val events = runBlocking {
            val session = createSession(user)
            val flow = session.subscribeConversation(conversation.id)
            session.sendStartTyping(conversation.id)
            delay(50)
            session.sendStopTyping(conversation.id)
            flow.drop(1).read(2)
        }

        with(events[0]) {
            assertNull(stopTyping)
            assertEquals(user.publicId, startTyping?.userId)
        }
        with(events[1]) {
            assertNull(startTyping)
            assertEquals(user.publicId, stopTyping?.userId)
        }
    }

    @Test
    fun receiveUserConnected_byAlreadyConnectedEmployeeOnConnect_works_forCustomer() = runBlocking {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)

        receiveUserConnectedByAlreadyConnectedUserOnConnectWorksForUser(conversation, user, employee)
    }

    @Test
    fun receiveUserConnected_byAlreadyConnectedUserOnConnect_works_forEmployee() = runBlocking {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)

        receiveUserConnectedByAlreadyConnectedUserOnConnectWorksForUser(conversation, employee, user)
    }

    @Test
    fun receiveUserConnected_byEmployeeOnConnect_works_forCustomer() = runBlocking {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)

        receiveUserConnectedByUserOnConnectWorksForUser(conversation, user, employee)
    }
    
    @Test
    fun receiveUserDisconnected_byEmployeeOnDisconnect_works_forCustomer() = runBlocking {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)

        receiveUserDisconnectedByUserOnDisconnectWorksForUser(conversation, user, employee)
    }

    @Test
    fun receiveUserDisconnected_byUserOnDisconnect_works_forEmployee() = runBlocking {
        val user = createCustomer(createLabel(), "c1")
        val employee = createEmployee("wsemployee")
        val conversation = conversationService.create(user)

        receiveUserDisconnectedByUserOnDisconnectWorksForUser(conversation, employee, user)
    }

    private suspend fun receiveUserDisconnectedByUserOnDisconnectWorksForUser(
        conversation: ConversationDto,
        subscribedUser: User,
        disconnectingUser: User
    ) {
        val disconnectingUserSession = createSession(disconnectingUser).also { it.subscribeConversation(conversation.id) }
        val flow = createSession(subscribedUser).subscribeConversation(conversation.id)
        disconnectingUserSession.disconnect()
        val event = flow.drop(2).readSingle()
        assertEquals(disconnectingUser.publicId, event.userDisconnected?.userId)
    }

    private suspend fun receiveUserConnectedByAlreadyConnectedUserOnConnectWorksForUser(
        conversation: ConversationDto,
        subscribedUser: User,
        alreadyConnectedUser: User
    ) {
        val subscribedUserSession = createSession(subscribedUser)
        createSession(alreadyConnectedUser).subscribeConversation(conversation.id)
        val events = subscribedUserSession.subscribeConversation(conversation.id).read(2)
        assertEquals(1, events.count { it.userConnected?.userId == subscribedUser.publicId })
            { "Missing connected event for own user" }
        assertEquals(1, events.count { it.userConnected?.userId == alreadyConnectedUser.publicId })
            { "Missing connected event for subscribing user" }
    }

    private suspend fun receiveUserConnectedByUserOnConnectWorksForUser(
        conversation: ConversationDto,
        subscribedUser: User,
        connectingUser: User
    ) {
        // This will send a 'connected' event for subscribedUser
        val flow = createSession(subscribedUser).subscribeConversation(conversation.id)

        // When the connectingUser subscribes, we will broadcast 'connected' events
        // for *both* users that are now subscribed the conversation
        createSession(connectingUser).subscribeConversation(conversation.id)

        val events = flow.read(2)
        with(events[0]) {
            assertEquals(userConnected?.userId, subscribedUser.publicId)
        }
        with(events[1]) {
            assertEquals(userConnected?.userId, connectingUser.publicId)
        }
    }
}
