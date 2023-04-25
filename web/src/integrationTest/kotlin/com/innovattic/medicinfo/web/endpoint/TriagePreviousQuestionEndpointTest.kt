package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

class TriagePreviousQuestionEndpointTest : BaseTriageEndpointTest() {

    @Test
    fun `given answer for two PRO questions, when calling previous, expect 400`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID), triageStatus.id, 2)
        val secondQuestion = nextQuestionToAsk(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID), triageStatus)
        saveAnswer(secondQuestion, triageStatus.id, 1)
        val thirdQuestion = nextQuestionToAsk(secondQuestion, triageStatus)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/questions/previous/" + thirdQuestion.uniqueQuestionId)
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given no answers to triage, when calling previous, expect 400`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/questions/previous/" + questionnaireModel.medicalAreaQuestion.uniqueQuestionId)
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given answer for medarea question, when calling previous for first KEELK question, expect medarea question and false on go back`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
            post("v1/triage/questions/previous/" + firstAreaQuestion.uniqueQuestionId)
        } Then {
//            validateAnswerResponse(questionSchema.medicalAreaQuestion, userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus))
            validatePrefilledAnswerRequest(questionnaireModel.medicalAreaQuestion,
                questionnaireModel.medicalAreaQuestion.answer.find { it.action.actionText == "KEELK" }?.id!!)
            body("isGoBackAllowed", equalTo(false))
        }
    }

    @Test
    fun `given answer for medarea question, when calling previous for medarea question, expect 400`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/questions/previous/" + questionnaireModel.medicalAreaQuestion.uniqueQuestionId)
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given answer for KEELK1, when calling previous for KEELK2, expect KEELK1 and true on go back`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/questions/previous/" + nextQuestionToAsk(firstAreaQuestion, triageStatus).uniqueQuestionId)
        } Then {
            validateAnswerResponse(firstAreaQuestion)
            validatePrefilledAnswerRequest(firstAreaQuestion, listOf(5))
            body("isGoBackAllowed", equalTo(true))
        }
    }

    @Test
    fun `given triage for others and answer for KEELK1, when calling previous for KEELK2, expect KEELK1 and true on go back`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(firstAreaQuestion, triageStatus.id, setOf(5))
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/questions/previous/" + nextQuestionToAsk(firstAreaQuestion, triageStatus).uniqueQuestionId)
        } Then {
            validateAnswerResponse(firstAreaQuestion, myself = false)
            validatePrefilledAnswerRequest(firstAreaQuestion, listOf(5))
            body("isGoBackAllowed", equalTo(true))
        }
    }

    @Test
    fun `given answer for KEELK4, when calling previous for KEELK2, expect KEELK1 and true on go back`() {
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
            post("v1/triage/questions/previous/" + secondAreaQuestion.uniqueQuestionId)
        } Then {
            validateAnswerResponse(firstAreaQuestion)
            validatePrefilledAnswerRequest(firstAreaQuestion, listOf(5))
            body("isGoBackAllowed", equalTo(true))
        }
    }

    @Test
    fun `given answer for KEELK4, when calling previous for non-answered, non-current question KEELK6, expect last answered KEELK4`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val firstAreaQuestion = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(firstAreaQuestion, triageStatus.id, 5)
        val secondAreaQuestion = nextQuestionToAsk(firstAreaQuestion, triageStatus)
        saveAnswer(secondAreaQuestion, triageStatus.id, 3)
        val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveMultiSelectionAnswer(keelk4Question, triageStatus.id, setOf(5))
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/questions/previous/KEELK6")
        } Then {
            validateAnswerResponse(keelk4Question)
            validatePrefilledAnswerRequest(keelk4Question, listOf(5))
            body("isGoBackAllowed", equalTo(true))
        }
    }

    @Test
    fun `given answer for KEELK1 when medarea was preanswered, when calling previous for KEELK2, expect KEELK1 and false on go back`() {
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
            post("v1/triage/questions/previous/" + nextQuestionToAsk(firstAreaQuestion, triageStatus).uniqueQuestionId)
        } Then {
            validateAnswerResponse(firstAreaQuestion)
            validatePrefilledAnswerRequest(firstAreaQuestion, listOf(5))
            body("isGoBackAllowed", equalTo(false))
        }
    }
}
