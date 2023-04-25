package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.test.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional

@Transactional
open class QuestionnaireModelServiceTest : BaseIntegrationTest() {

    // TODO: these tests shouldn't be dependent on specific json versions being present
    val VERSION_1 = 95
    val VERSION_2 = 97

    @Test
    open fun `given two active triages, expect two versions to be loaded`() {

        val label = createLabel()
        val user1 = createCustomer(label, "c1")
        val user2 = createCustomer(label, "c2")
        val conversation1 = conversationDao.create(user1.id, user1.labelId, ConversationStatus.OPEN)
        val conversation2 = conversationDao.create(user2.id, user2.labelId, ConversationStatus.OPEN)
        triageStatusDao.createTriageStatus(user1.id, VERSION_1, conversation1.id)
        triageStatusDao.createTriageStatus(user2.id, VERSION_2, conversation2.id)

        //reinit schema
        questionSchemaService.initMap()

        val questionSchemaV1 = questionSchemaService.getQuestionSchema(VERSION_1)
        val questionSchemaV2 = questionSchemaService.getQuestionSchema(VERSION_2)

        assertNotNull(questionSchemaV1)
        assertNotNull(questionSchemaV2)
    }

    @Test
    open fun `given one active triage with an older version, expect that version and the latest version to be present`() {
        val label = createLabel()
        val user1 = createCustomer(label, "c1")
        val conversation1 = conversationDao.create(user1.id, user1.labelId, ConversationStatus.OPEN)
        triageStatusDao.createTriageStatus(user1.id, VERSION_1, conversation1.id)

        //reinit schema
        questionSchemaService.initMap()

        val questionSchemaOlder = questionSchemaService.getQuestionSchema(VERSION_1)
        val questionSchemaLatest = questionSchemaService.getLatestSchema()

        assertNotNull(questionSchemaOlder)
        assertTrue(questionSchemaLatest.version > VERSION_1)
    }

    @Test
    open fun `given no active triages, expect only latest version present`() {
        //reinit schema
        questionSchemaService.initMap()

        val questionSchemaV1 = questionSchemaService.getQuestionSchema(1)
        val questionSchemaLatest = questionSchemaService.getLatestSchema()

        assertNull(questionSchemaV1)
        assertNotNull(questionSchemaLatest)
        assertTrue(questionSchemaLatest.version > 1)
    }
}
