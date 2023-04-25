package com.innovattic.medicinfo.web.endpoint

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.logic.dto.triage.MultipleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.SingleChoiceAnswer
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

class TriagePreviousAnswerEndpointTest : BaseTriageEndpointTest() {

    @Test
    fun `given KEELK4 question answered, when reanswering medarea with change, expect HUIDU1 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        val secondAreaQuestion = nextQuestionToAsk(firstAreaQuestion, triageStatus)
        saveAnswer(secondAreaQuestion, triageStatus.id, 3)
        val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveAnswer(keelk4Question, triageStatus.id, 5)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(
                questionnaireModel.medicalAreaQuestion.uniqueQuestionId,
                questionnaireModel.medicalAreaQuestion.answer.find { it.action.actionText == "HUIDU" }!!.id
            )
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            validateAnswerResponse(questionnaireModel.findFirstQuestion("HUIDU"))
            body("isGoBackAllowed", equalTo(true))
            body("nextAnswer.answer", equalTo(null))
        }
    }

    @Test
    fun `given KEELK4 question answered, when reanswering medarea without changes, expect KEELK1 returned with prefilled answer`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        val secondAreaQuestion = nextQuestionToAsk(firstAreaQuestion, triageStatus)
        saveAnswer(secondAreaQuestion, triageStatus.id, 3)
        val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveAnswer(keelk4Question, triageStatus.id, 5)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(
                questionnaireModel.medicalAreaQuestion.uniqueQuestionId,
                questionnaireModel.medicalAreaQuestion.answer.find { it.action.actionText == "KEELK" }!!.id
            )
            body(singleChoiceAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            validateAnswerResponse(firstAreaQuestion)
            body("isGoBackAllowed", equalTo(true))
            validatePrefilledAnswerRequest(firstAreaQuestion, listOf(5))
        }
    }

    @Test
    fun `given KEELK4 question answered, when reanswering KEELK4 without changes, expect KEELK5 returned with no prefilled answer`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        val secondAreaQuestion = nextQuestionToAsk(firstAreaQuestion, triageStatus)
        saveAnswer(secondAreaQuestion, triageStatus.id, 3)
        val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveMultiSelectionAnswer(keelk4Question, triageStatus.id, setOf(5))
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val multipleChoiceAnswer = MultipleChoiceAnswer(keelk4Question.uniqueQuestionId, setOf(5))
            body(multipleChoiceAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            val keelk5Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK5")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(keelk5Question)
            body("isGoBackAllowed", equalTo(true))
            body("nextAnswer.answer", equalTo(null))
        }
    }

    @Test
    fun `given KEELK4 question answered, when trying to answer not answered, non-current question, expect 500`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        val secondAreaQuestion = nextQuestionToAsk(firstAreaQuestion, triageStatus)
        saveAnswer(secondAreaQuestion, triageStatus.id, 3)
        val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveAnswer(keelk4Question, triageStatus.id, 5)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val keelk9Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9")
            val singleChoiceAnswer = SingleChoiceAnswer(keelk9Question.uniqueQuestionId, 1)
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given KEELK4 question answered, when trying to answer PRO question, expect 500`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        val secondAreaQuestion = nextQuestionToAsk(firstAreaQuestion, triageStatus)
        saveAnswer(secondAreaQuestion, triageStatus.id, 3)
        val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveAnswer(keelk4Question, triageStatus.id, 5)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(questionnaireModel.genderProfileQuestion.uniqueQuestionId, 1)
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given KEELK1 question answered, when trying to answer medarea question that was prefilled, expect 500`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        triageStatusDao.continueTriage(triageStatus.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(
                questionnaireModel.medicalAreaQuestion.uniqueQuestionId,
                questionnaireModel.medicalAreaQuestion.answer.find { it.action.actionText == "HUIDU" }!!.id
            )
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }
}
