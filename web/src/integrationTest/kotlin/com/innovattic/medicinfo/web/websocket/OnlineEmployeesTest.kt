package com.innovattic.medicinfo.web.websocket

import com.innovattic.medicinfo.web.BaseWebSocketTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class OnlineEmployeesTest : BaseWebSocketTest() {
    @Test
    fun receiveOnlineEmployees_works_whenNumberChanges() {
        onlineEmployeeService.pushNewValue(1)
        clock.lockTime()
        val user = createEmployee("e1")
        val flow = runBlocking { createSession(user).subscribeOnline() }
        onlineEmployeeService.pushNewValue(2)
        val message = runBlocking { flow.readSingle() }
        assertEquals(2, message.online)
    }
}
