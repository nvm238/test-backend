package com.innovattic.medicinfo.web

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.logic.DUTCH_LOCALE
import com.innovattic.medicinfo.logic.dto.salesforce.UserContactDataSalesforceResponse
import com.innovattic.medicinfo.logic.triage.CloseUnusedTriage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import java.time.LocalDate
import java.time.LocalDateTime

class TriageAutomaticFinishTest : BaseTriageEndpointTest() {

    @BeforeEach
    fun beforeEach() {
        doReturn(questionSchemaService.getQuestionSchema(0)).`when`(questionSchemaService).getLatestSchema()
        clock.reset()
    }

    @AfterEach
    fun afterEach() {
        clock.reset()
    }

    @Test
    fun `given 2 outdated triages and 1 not, when force closing, expect 2 triages force closed and 1 left intact`() {
        // clean up triages that might get generated by other tests

        closeAllTriages()
        val label = createLabel()
        val user1 = createCustomer(label, "c1")
        val user2 = createCustomer(label, "c2")
        val user3 = createCustomer(label, "c3")

        doAnswer {
            UserContactDataSalesforceResponse(LocalDate.of(2000, 3, 1), false, user1.publicId)
        }.`when`(salesforceService).getCustomerContactData(user1.publicId)
        doAnswer {
            UserContactDataSalesforceResponse(LocalDate.of(2000, 3, 1), false, user2.publicId)
        }.`when`(salesforceService).getCustomerContactData(user2.publicId)
        doAnswer {
            UserContactDataSalesforceResponse(LocalDate.of(2000, 3, 1), false, user3.publicId)
        }.`when`(salesforceService).getCustomerContactData(user3.publicId)


        clock.setTime(LocalDateTime.of(2022, 7, 21, 15, 0,0))
        val triage1 = triageService.startTriage(user1, null, DUTCH_LOCALE)
        clock.setTime(LocalDateTime.of(2022, 7, 21, 15, 0,0))
        val triage2 = triageService.startTriage(user2, null, DUTCH_LOCALE)
        clock.setTime(LocalDateTime.of(2022, 7, 22, 15, 0,0))
        val triage3 = triageService.startTriage(user3, null, DUTCH_LOCALE)
        clock.setTime(LocalDateTime.of(2022, 7, 24, 9, 0,0))

        CloseUnusedTriage(triageStatusDao, triageService, conversationDao).closeOldTriages()

        val conversation1 = conversationDao.get(triage1.conversation!!.id)!!
        val triageStatus1 = triageStatusDao.getByConversationId(conversation1.id)!!
        val conversation2 = conversationDao.get(triage2.conversation!!.id)!!
        val triageStatus2 = triageStatusDao.getByConversationId(conversation2.id)!!
        val conversation3 = conversationDao.get(triage3.conversation!!.id)!!
        val triageStatus3 = triageStatusDao.getByConversationId(conversation3.id)!!

        assertEquals(TriageProgress.FORCE_FINISHED, triageStatus1.status)
        assertFalse(triageStatus1.active)
        assertEquals(ConversationStatus.ARCHIVED, conversation1.status)

        assertEquals(TriageProgress.FORCE_FINISHED, triageStatus2.status)
        assertFalse(triageStatus2.active)
        assertEquals(ConversationStatus.ARCHIVED, conversation2.status)

        assertEquals(TriageProgress.STARTED, triageStatus3.status)
        assertTrue(triageStatus3.active)
        assertEquals(ConversationStatus.OPEN, conversation3.status)
    }

    private fun closeAllTriages() {
        triageStatusDao.getAllActiveOlderThen(0)
            .forEach {
                triageStatusDao.endTriageStatus(it.id, TriageProgress.FINISHED)
            }
    }

}