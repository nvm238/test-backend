package com.innovattic.medicinfo.web.websocket

import com.innovattic.medicinfo.web.BaseWebSocketTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class HeartBeatWebSocketTest : BaseWebSocketTest() {
    @Test
    fun receiveHeartBeat_works_forCustomer() {
        clock.lockTime()
        val user = createCustomer(createLabel(), "c1")
        val flow = runBlocking { createSession(user).subscribeHeartbeat() }
        heartBeatService.sendHeartBeat()
        val message = runBlocking { flow.readSingle() }
        assertEquals(ZonedDateTime.now(clock), message.timestamp)
    }

    @Test
    fun receiveHeartBeat_works_forEmployee() {
        clock.lockTime()
        val user = createEmployee("e1")
        val flow = runBlocking { createSession(user).subscribeHeartbeat() }
        heartBeatService.sendHeartBeat()
        val message = runBlocking { flow.readSingle() }
        assertEquals(ZonedDateTime.now(clock), message.timestamp)
    }
}
