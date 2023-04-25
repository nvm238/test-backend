package com.innovattic.medicinfo.web.endpoint

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.logic.dto.triage.StopReason
import com.innovattic.medicinfo.logic.dto.triage.StopTriageRequest
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TriageStopEndpointTest : BaseTriageEndpointTest() {

    @Test
    fun `given triage, when stopping triage without chat, expect triage stopped in database and reason filled and conversation archived`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val givenTriageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        val stopReason = "I do not want to chat"
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val reason = StopTriageRequest(false, stopReason)
            body(jacksonObjectMapper().writeValueAsString(reason))
            post("v1/triage/stop")
        } Then {
            statusCode(200)
        }

        val updatedTriageStatus = triageStatusDao.getById(givenTriageStatus.id)
            ?: throw IllegalStateException("Triage status does not exist")
        val updatedConversation = conversationDao.get(conversation.id)
            ?: throw IllegalStateException("Conversation does not exist")

        assertEquals(TriageProgress.FINISHED_BY_USER_WITHOUT_CHAT, updatedTriageStatus.status)
        assertEquals(stopReason, updatedTriageStatus.stopReason)
        assertEquals(ConversationStatus.ARCHIVED, updatedConversation.status)
    }

    @Test
    fun `given triage, when stopping triage with chat, expect triage stopped in database and reason null`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val givenTriageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val reason = StopTriageRequest(true, null, StopReason.WANTS_CHAT)
            body(jacksonObjectMapper().writeValueAsString(reason))
            post("v1/triage/stop")
        } Then {
            statusCode(200)
        }

        val updatedTriageStatus = triageStatusDao.getById(givenTriageStatus.id)
            ?: throw IllegalStateException("Triage status does not exist")
        val updatedConversation = conversationDao.get(conversation.id)
            ?: throw IllegalStateException("Conversation does not exist")

        assertEquals(TriageProgress.FINISHED_BY_USER_WITH_CHAT, updatedTriageStatus.status)
        assertEquals(null, updatedTriageStatus.stopReason)
        assertEquals(ConversationStatus.OPEN, updatedConversation.status)
    }

    @Test
    fun `given triage, when stopping triage because medarea is not in the list, expect triage stopped in database and reason null`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val givenTriageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val reason = StopTriageRequest(true, null, StopReason.NO_MEDICAL_AREA)
            body(jacksonObjectMapper().writeValueAsString(reason))
            post("v1/triage/stop")
        } Then {
            statusCode(200)
        }

        val updatedTriageStatus = triageStatusDao.getById(givenTriageStatus.id)
            ?: throw IllegalStateException("Triage status does not exist")
        val updatedConversation = conversationDao.get(conversation.id)
            ?: throw IllegalStateException("Conversation does not exist")

        assertEquals(TriageProgress.FINISHED_BY_USER_NO_MEDAREA, updatedTriageStatus.status)
        assertEquals(null, updatedTriageStatus.stopReason)
        assertEquals(ConversationStatus.OPEN, updatedConversation.status)
    }
}
