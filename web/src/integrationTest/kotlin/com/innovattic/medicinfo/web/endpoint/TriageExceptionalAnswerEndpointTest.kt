package com.innovattic.medicinfo.web.endpoint

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.logic.dto.triage.BooleanAnswer
import com.innovattic.medicinfo.logic.dto.triage.SkipAnswer
import com.innovattic.medicinfo.logic.dto.triage.UncertainAnswer
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

class TriageExceptionalAnswerEndpointTest : BaseTriageEndpointTest() {

    @Test
    fun `given skip answer for PRO1 which is required, expect http status 500`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val firstProfileQuestion = questionnaireModel.whoProfileQuestion
            val skipAnswer = SkipAnswer(firstProfileQuestion.uniqueQuestionId)
            body(jacksonObjectMapper().writeValueAsString(skipAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given uncertain answer for PRO1 which is not allowed for that question, expect http status 500`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val firstProfileQuestion = questionnaireModel.whoProfileQuestion
            val skipAnswer = UncertainAnswer(firstProfileQuestion.uniqueQuestionId)
            body(jacksonObjectMapper().writeValueAsString(skipAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given skip answer for KEELK9A, expect KEELK6 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk9Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9")
        saveAnswer(keelk9Question, triageStatus.id, 1)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val keelk9aQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9A")
            val skipAnswer = SkipAnswer(keelk9aQuestion.uniqueQuestionId)
            body(jacksonObjectMapper().writeValueAsString(skipAnswer))
            post("v1/triage/answers")
        } Then {
            val keelk6Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(keelk6Question, false)
        }
    }

    @Test
    fun `given uncertain answer for KEELK6, expect KEELK7 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk9aQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9A")
        saveAnswer(keelk9aQuestion, triageStatus.id, "abc description")

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val keelk6Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6")
            val uncertainAnswer = UncertainAnswer(keelk6Question.uniqueQuestionId)
            body(jacksonObjectMapper().writeValueAsString(uncertainAnswer))
            post("v1/triage/answers")
        } Then {
            val keelk7Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK10A")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(keelk7Question, false)
        }
    }

    @Test
    fun `when answering INSTRUCTION question with skipping, expect next question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val skipAnswer = SkipAnswer(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID).uniqueQuestionId)
            body(jacksonObjectMapper().writeValueAsString(skipAnswer))
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextQuestionToAsk(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID), triageStatus))
        }
    }

    @Test
    fun `when answering BOOLEAN question, expect next question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)

        val booleanQuestion = questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, "TRIAGEOTHER_AUTHORIZED")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val boolAnswer = BooleanAnswer(booleanQuestion.uniqueQuestionId, 1)
            body(jacksonObjectMapper().writeValueAsString(boolAnswer))
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextQuestionToAsk(booleanQuestion, triageStatus))
        }
    }
}
