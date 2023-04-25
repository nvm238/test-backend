package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.logic.dto.triage.SingleChoiceAnswer
import com.innovattic.medicinfo.logic.triage.model.Action
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.Answer
import com.innovattic.medicinfo.logic.triage.model.QuestionType
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireDefinition
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TriageGoToQuestionActionTest : BaseTriageEndpointTest() {

    @BeforeAll
    override fun readSchema() {
        super.readSchema()

        val q3 = createNumberQuestion("q3", 3, ActionType.FINISH, 0, nextQuestion = null)
        val q2 = createNumberQuestion("q2", 2, ActionType.FINISH, 0, nextQuestion = null)
        val q1 = createQuestion(
            "q1",
            1,
            true,
            false,
            QuestionType.SINGLE_SELECTION,
            listOf(
                Answer(
                    1,
                    "a1",
                    false,
                    Action(ActionType.GO_TO_QUESTION, -1, null, "q3", nextQuestion = q3)
                ),
                Answer(
                    2,
                    "a2",
                    false,
                    Action(ActionType.GO_TO_QUESTION, -1, null, "q3", nextQuestion = q3)
                )
            )
        )
        // add a test questionnaire with questions
        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(TEST_QUESTIONNAIRE, "Test", listOf(q1, q2, q3))
        )
    }

    @Test
    fun `given user, when answering question with GO_TO_QUESTION action, expect question q3`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaAnswer(triageStatus.id, TEST_QUESTIONNAIRE)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val firstTestQuestion = questionnaireModel.findFirstQuestion(TEST_QUESTIONNAIRE)
            val answer = SingleChoiceAnswer(firstTestQuestion.uniqueQuestionId, 1)
            body(answer.toJson())
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            validateAnswerResponse(
                questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q3")
            )
        }
    }
}
