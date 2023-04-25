package com.innovattic.medicinfo.web

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.triage.BooleanAnswer
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.OTHER_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireDefinition
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TriageReportingTest : BaseTriageEndpointTest() {

    lateinit var user: User
    lateinit var conversation: Conversation

    @BeforeAll
    override fun readSchema() {
        super.readSchema()

        // add a test questionnaire with two image questions
        val q2 = createBooleanQuestion("q2", 1, ActionType.FINISH, 0, null)
        val q1 = createBooleanQuestion("q1", 0, ActionType.GO_TO_QUESTION, 0, q2)
        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(TEST_QUESTIONNAIRE, "Test", listOf(q1, q2))
        )
    }

    private fun startTestQuestionnaire(triageStatus: TriageStatus) {
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaAnswer(triageStatus.id, TEST_QUESTIONNAIRE)
    }

    @Test
    fun finishTriageAddsReporting() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)

        triageService.saveAnswer(
            user,
            BooleanAnswer("q1", 1),
            true
        )

        // submit question
        Given {
            auth().oauth2(token)
        } When {
            val answer = BooleanAnswer("q2", 1)
            body(answer)
            post("v1/triage/answers")
        }

        // check for db contents, test some properties
        val reportedRow = triageReportingDao.getTriage(triageStatus.id)
        assertEquals(reportedRow.isSelfTriage, false)
        assertEquals(reportedRow.endReason, null)
        assertEquals(reportedRow.customerId, user.publicId)
        assertEquals(reportedRow.triageState, TriageProgress.FINISHED)
        val reportedAnswers = triageReportingDao.getAnswers(triageStatus.id)
        val q1answer = reportedAnswers.find { it.questionId == "q1" }
        assertNotNull(q1answer)
        q1answer!!
        assertEquals(q1answer.answerText, "Ja")
        assertEquals(q1answer.divergent, false)
    }

    @Test
    fun testReportedProgressValue() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)
        triageService.saveAnswer(
            user,
            BooleanAnswer("q1", 1),
            true
        )
        // submit question
        Given {
            auth().oauth2(token)
        } When {
            val answer = BooleanAnswer("q2", 1)
            body(answer)
            post("v1/triage/answers")
        }

        // check for db contents, test some properties
        val reportedRow = triageReportingDao.getTriage(triageStatus.id)
        assertEquals(100, reportedRow.progressPercentage, )
    }

    @Test
    fun testReportContainsOtherQuestionnaireFromOtherQuestionnaireBooleanReturnsTrue() {
        val oq2 = createBooleanQuestion(
            "oq2",
            1,
            ActionType.FINISH,
            0,
            null,
            questionnaireID = OTHER_QUESTIONNAIRE_ID
        )
        val oq1 = createBooleanQuestion(
            "oq1",
            0,
            ActionType.GO_TO_QUESTION,
            0, oq2,
            questionnaireID = OTHER_QUESTIONNAIRE_ID
        )
        oq1.questionCategory
        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(OTHER_QUESTIONNAIRE_ID, "Overige", listOf(oq1, oq2))
        )

        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaOVERIGAnswer(triageStatus.id)

        triageService.saveAnswer(
            user,
            BooleanAnswer("oq1", 1),
            true
        )
        // submit question
        Given {
            auth().oauth2(token)
        } When {
            val answer = BooleanAnswer("oq2", 1)
            body(answer)
            post("v1/triage/answers")
        }


        // check for db contents, test some properties
        val reportedRow = triageReportingDao.getTriage(triageStatus.id)
        assertTrue(reportedRow.filledQuestionnaireOther)
    }

}
