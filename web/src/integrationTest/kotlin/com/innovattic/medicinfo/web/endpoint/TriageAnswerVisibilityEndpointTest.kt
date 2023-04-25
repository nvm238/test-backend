package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.logic.dto.triage.DateAnswer
import com.innovattic.medicinfo.logic.dto.triage.StringAnswer
import com.innovattic.medicinfo.logic.triage.model.Action
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.Answer
import com.innovattic.medicinfo.logic.triage.model.BIRTHDAY_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.ModelMapper
import com.innovattic.medicinfo.logic.triage.model.QuestionType
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireDefinition
import com.innovattic.medicinfo.logic.triage.model.external.ConditionType
import com.innovattic.medicinfo.logic.triage.model.external.SingleValueCondition
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TriageAnswerVisibilityEndpointTest : BaseTriageEndpointTest() {

    @BeforeAll
    override fun readSchema() {
        super.readSchema()

        val q3 = createNumberQuestion("q3", 2, ActionType.FINISH, 0, nextQuestion = null)
        val q2 = createQuestion(
            "q2",
            1,
            true,
            false,
            QuestionType.SINGLE_SELECTION,
            listOf(
                Answer(
                    1,
                    "a1",
                    false,
                    Action(ActionType.GO_TO_QUESTION, -1, null, "q3", nextQuestion = q3),
                    listOf(
                        ModelMapper.parsePredicate(
                            SingleValueCondition(
                                ConditionType.SEX,
                                "=",
                                "female"
                            )
                        )
                    )
                ),
                Answer(
                    2,
                    "a2",
                    false,
                    Action(ActionType.GO_TO_QUESTION, -1, null, "q3", nextQuestion = q3),
                    listOf(
                        ModelMapper.parsePredicate(
                            SingleValueCondition(
                                ConditionType.SEX,
                                "=",
                                "female"
                            )
                        )
                    )
                ),
                Answer(
                    3,
                    "a3",
                    false,
                    Action(ActionType.GO_TO_QUESTION, -1, null, "q3", nextQuestion = q3),
                    listOf(ModelMapper.parsePredicate(SingleValueCondition(ConditionType.SEX, "=", "male")))
                )
            )
        )
        val q1 = createNumberQuestion("q1", 0, ActionType.GO_TO_QUESTION, 0, nextQuestion = q2)
        // add a test questionnaire with questions
        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(TEST_QUESTIONNAIRE, "Test", listOf(q1, q2, q3))
        )
    }

    @Test
    fun `given user, when answering question with answer visibility conditions, expect only some answers shown`() {
        val label = createLabel()
        val user = createCustomer(label, "c1", gender = Gender.FEMALE)
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        saveAnswer(questionnaireModel.genderProfileQuestion, triageStatus.id, 1)
        saveAnswer(
            BIRTHDAY_QUESTION_UNIQUE_ID,
            triageStatus.id,
            DateAnswer(BIRTHDAY_QUESTION_UNIQUE_ID, LocalDate.of(2000, 4, 10))
        )
        saveMedicalAreaAnswer(triageStatus.id, TEST_QUESTIONNAIRE)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val firstTestQuestion = questionnaireModel.findFirstQuestion(TEST_QUESTIONNAIRE)
            val answer = StringAnswer(firstTestQuestion.uniqueQuestionId, "1")
            body(answer.toJson())
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            validateQuestionResponse(
                questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2"),
                false,
                answerQuestionObjectName
            )
            validateAnswer(
                0,
                questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2").answer.find { it.id == 1 }
                    ?: throw IllegalStateException("No answer"),
                answerQuestionObjectName)
            validateAnswer(
                1,
                questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2").answer.find { it.id == 2 }
                    ?: throw IllegalStateException("No answer"),
                answerQuestionObjectName)
        }
    }

}
