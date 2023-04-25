package com.innovattic.medicinfo.web.endpoint

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.CustomerEntryType
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.logic.dto.triage.AnswerActionResponse
import com.innovattic.medicinfo.logic.dto.triage.BooleanAnswer
import com.innovattic.medicinfo.logic.dto.triage.DateAnswer
import com.innovattic.medicinfo.logic.dto.triage.ImagesAnswer
import com.innovattic.medicinfo.logic.dto.triage.MultipleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.SingleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.StringAnswer
import com.innovattic.medicinfo.logic.dto.triage.UncertainAnswer
import com.innovattic.medicinfo.logic.triage.model.Action
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.Answer
import com.innovattic.medicinfo.logic.triage.model.BIRTHDAY_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.BSN_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.QuestionType
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireDefinition
import com.innovattic.medicinfo.logic.triage.model.external.ConditionType
import com.innovattic.medicinfo.logic.triage.model.external.ContainsValueCondition
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TriageAnswerEndpointTest : BaseTriageEndpointTest() {

    private val MULTI_SELECT_QUESTIONNAIRE = "multi"

    @BeforeAll
    override fun readSchema() {
        super.readSchema()

        val q3 = createNumberQuestion("q3", 2, ActionType.FINISH, 0, nextQuestion = null)
        val q2 = createNumberQuestion(
            "q2",
            1,
            ActionType.GO_TO_QUESTION,
            0,
            listOf(ContainsValueCondition(ConditionType.LABEL, "in", listOf("noni"))),
            nextQuestion = q3
        )
        val q1 = createNumberQuestion("q1", 0, ActionType.GO_TO_QUESTION, 0, nextQuestion = q2)
        // add a test questionnaire with questions
        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(TEST_QUESTIONNAIRE, "Test", listOf(q1, q2, q3)),
        )
    }

    private fun createMultiSelectQuestionnaire() {

        val q6 = createNumberQuestion("q6", 5, ActionType.FINISH, -1, nextQuestion = null, questionnaireID = MULTI_SELECT_QUESTIONNAIRE)
        val q5 = createQuestion("q5", 4, true, false,QuestionType.BOOLEAN,
            answers= listOf(
                Answer(
                    1, "Ja", false, Action(
                        actionType = ActionType.GO_TO_QUESTION,
                        urgency = -1,
                        urgencyName = "",
                        actionText = "RETURN",
                        nextQuestion = q6
                    )
                ),
                Answer(
                    1, "Nee", false, Action(
                        actionType = ActionType.GO_TO_QUESTION,
                        urgency = -1,
                        urgencyName = "",
                        actionText = "RETURN",
                        nextQuestion = q6
                    )
                )
            ), questionnaireID = MULTI_SELECT_QUESTIONNAIRE)
        val q4 = createNumberQuestion("q4", 3, ActionType.GO_TO_QUESTION, -1, nextQuestion = q5, questionnaireID = MULTI_SELECT_QUESTIONNAIRE)
        val q3 = createQuestion("q3", 2, true,false,QuestionType.BOOLEAN,
            answers =
            listOf(
                Answer(
                    1, "Ja", false, Action(
                        actionType = ActionType.GO_TO_QUESTION,
                        urgency = -1,
                        urgencyName = "",
                        actionText = "RETURN",
                        nextQuestion = null
                    )
                ),
                Answer(
                    1, "Nee", false, Action(
                        actionType = ActionType.ASK_FOR_CHAT,
                        urgency = 1,
                        urgencyName = "U1",
                        actionText = "RETURN",
                        nextQuestion = null
                    )
                )
            ),
            questionnaireID = MULTI_SELECT_QUESTIONNAIRE
        )
        val q2 = createMultiSelectionQuestion("q2", 1, MULTI_SELECT_QUESTIONNAIRE, answers = listOf(
            Answer(1, "Go to Question 4", false, Action(
                actionType = ActionType.GO_TO_QUESTION,
                urgency = -1,
                urgencyName = "",
                actionText = "q4",
                nextQuestion = q4
            )),
            Answer(2, "Go to Question 5", false, Action(
                actionType = ActionType.GO_TO_QUESTION,
                urgency = -1,
                urgencyName = "",
                actionText = "q5",
                nextQuestion = q5
            ))
        ), nextMainAction = Action(ActionType.GO_TO_QUESTION, -1, "", "RETURN", null)
        )
        val q1 = createMultiSelectionQuestion("q1", 0, MULTI_SELECT_QUESTIONNAIRE, answers = listOf(
            Answer(1, "Go To Question 2", false, Action(
                actionType = ActionType.GO_TO_QUESTION,
                urgency = -1,
                urgencyName = "",
                actionText = "q2",
                nextQuestion = q2
            )),
            Answer(2, "Go To Question 6", false, Action(
                actionType = ActionType.GO_TO_QUESTION,
                urgency = -1,
                urgencyName = "",
                actionText = "RETURN",
                nextQuestion = null
            )),
            Answer(3, "Go To Question 3", false, Action(
                actionType = ActionType.GO_TO_QUESTION,
                urgency = -1,
                urgencyName = "",
                actionText = "q3",
                nextQuestion = q3
            ))
        ), nextMainAction = Action(ActionType.GO_TO_QUESTION, -1, "", "Go to Question 6", q6)
        )

        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(MULTI_SELECT_QUESTIONNAIRE, "Multi select Test", listOf(q1, q2, q3, q4, q5, q6))
        )
    }

    @Test
    fun `given single choice answer for PRO1, expect last profile question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID), triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(questionnaireModel.whoProfileQuestion.uniqueQuestionId, 1)
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            vaildateFemaleMedareaAnswers(true, answerQuestionObjectName)
        }
    }

    @Test
    fun `when answering multiple choice answer for KEELK4, expect KEELK5 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk2Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK3")
        saveAnswer(keelk2Question, triageStatus.id, 3)
        val keelk3Question = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG7A")
        saveAnswer(keelk3Question, triageStatus.id, 4)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
            val multipleChoiceAnswer = MultipleChoiceAnswer(keelk4Question.uniqueQuestionId, setOf(1, 2, 3))
            body(jacksonObjectMapper().writeValueAsString(multipleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            val keelk5Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK5")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(keelk5Question)
        }
    }

    @Test
    fun `given answer for KEELK3 with answer that has action type GO_TO_CHAT, when trying to answer next question, expect 400 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk3Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK3")
        saveAnswer(keelk3Question, triageStatus.id, 2)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
            val multipleChoiceAnswer = MultipleChoiceAnswer(keelk4Question.uniqueQuestionId, setOf(1, 2, 3))
            body(jacksonObjectMapper().writeValueAsString(multipleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given answer for KEELK1, when answering KEELK2 with answer with action type GO_TO_CHAT, expect ending response`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(keelk1Question, triageStatus.id, setOf(5))
        val keelk2Question = nextQuestionToAsk(keelk1Question, triageStatus)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(keelk2Question.uniqueQuestionId, 1)
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("GO_TO_CHAT"))
            body("nextQuestion", equalTo(null))
        }
    }

    @Test
    @DisplayName(
        "given answer for KEELK1 and continuation support flag, when answering" +
            " KEELK2 with answer with action type ASK_FOR_CHAT, expect next askable question in the list (ALG7A) and action ASK_FOR_CHAT"
    )
    fun expectContinuation() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveMultiSelectionAnswer(keelk1Question, triageStatus.id, setOf(5))
        val keelk2Question = nextQuestionToAsk(keelk1Question, triageStatus)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(keelk2Question.uniqueQuestionId, 1)
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers?supportsContinuation=true")
        } Then {
            body("action", equalTo(AnswerActionResponse.ASK_FOR_CHAT.toString()))
            validateAnswerResponse(questionnaireModel.getQuestionByUniqueId("KEELK", "ALG7"), true)
        }
    }

    @Test
    fun `given triage answered for others, when answering TRIAGEOTHER_AUTHORIZED with Nee, expect ending response UNAUTHORIZED`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val authorizedQuestion =
                questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, "TRIAGEOTHER_AUTHORIZED")
            val booleanAnswer = BooleanAnswer(authorizedQuestion.uniqueQuestionId, 2)
            body(jacksonObjectMapper().writeValueAsString(booleanAnswer))
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("UNAUTHORIZED"))
            body("nextQuestion", equalTo(null))
        }
    }

    @Test
    fun `given triage answered for others, when answering answering question preceding medarea, expect medarea with question for others`() {
        val label = getOrCreateLabel("NONI")
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        saveAnswer(questionnaireModel.genderProfileQuestion, triageStatus.id, 1)
        saveAnswer(
            questionnaireModel.birthdayProfileQuestion.uniqueQuestionId,
            triageStatus.id,
            DateAnswer(BIRTHDAY_QUESTION_UNIQUE_ID, LocalDate.of(2000, 4, 10))
        )
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val bsnQuestion = questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, BSN_QUESTION_UNIQUE_ID)
            val stringAnswer = StringAnswer(bsnQuestion.uniqueQuestionId, "123456789")
            body(stringAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            vaildateFemaleMedareaAnswers(false, answerQuestionObjectName)
        }
    }

    @Test
    fun `given triage answered for others and proposition is foreign, expect optional bsn question`() {
        val label = getOrCreateLabel("NONI")
        val user = createCustomer(label, "c1")
        userDao.updateCustomersOnboardingDetails(user.id, CustomerEntryType.HOLIDAY_FOREIGN_TOURIST.salesforceTranslation, null, null, null, null, "Amsterdam", null, null)
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        saveAnswer(questionnaireModel.genderProfileQuestion, triageStatus.id, 1)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val stringAnswer = DateAnswer(BIRTHDAY_QUESTION_UNIQUE_ID, LocalDate.of(2000, 4, 10))
            body(stringAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            body("nextQuestion.isRequired", equalTo(false))
        }
    }

    @Test
    fun `given triage answered for others and proposition is not foreign, expect optional bsn question`() {
        val label = getOrCreateLabel("NONI")
        val user = createCustomer(label, "c1")
        userDao.updateCustomersOnboardingDetails(user.id, CustomerEntryType.HOLIDAY_TOURIST.salesforceTranslation, null, null, null, null, "Amsterdam", null, null)

        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        saveAnswer(questionnaireModel.genderProfileQuestion, triageStatus.id, 1)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val stringAnswer = DateAnswer(BIRTHDAY_QUESTION_UNIQUE_ID, LocalDate.of(2000, 4, 10))
            body(stringAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            body("nextQuestion.isRequired", equalTo(true))
        }
    }

    @Test
    @DisplayName(
        "given triage answered for others," +
            " when answering answering question preceding medarea," +
            " expect medarea with question for others with alternative texts"
    )
    fun `expect answer enhanced with metadata`() {
        val label = getOrCreateLabel("NONI")
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        saveAnswer(questionnaireModel.genderProfileQuestion, triageStatus.id, 1)
        saveAnswer(
            questionnaireModel.birthdayProfileQuestion.uniqueQuestionId,
            triageStatus.id,
            DateAnswer(BIRTHDAY_QUESTION_UNIQUE_ID, LocalDate.of(2000, 4, 10))
        )
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val bsnQuestion = questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, BSN_QUESTION_UNIQUE_ID)
            val stringAnswer = StringAnswer(bsnQuestion.uniqueQuestionId, "123456789")
            body(stringAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            vaildateFemaleMedareaAnswers(false, answerQuestionObjectName)
            body("$answerQuestionObjectName.answers[0].alternativeNames[1]", equalTo("Anticonceptiepil"))
            body("$answerQuestionObjectName.answers[0].alternativeNames[2]", equalTo("vergeten"))
            body("$answerQuestionObjectName.answers[0].alternativeNames[3]", equalTo("Anticonceptie"))
        }
    }

    @Test
    fun `given answer for one before last KEELK question, when answering last question, expect ending response`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val oneBeforeLastKeelkQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ADDITIONALQ")
        saveAnswer(oneBeforeLastKeelkQuestion, triageStatus.id, "No")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val lastKeelkQuestion = questionnaireModel.findLastQuestion("KEELK")
            val stringAnswer = StringAnswer(lastKeelkQuestion.uniqueQuestionId, "last question answer")
            body(stringAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("FINISH"))
            body("nextQuestion", equalTo(null))
        }
    }

    @Test
    fun `given user with some label, when answering question with label condition for different label than user's, expect question skipped`() {
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
            val answer = StringAnswer(firstTestQuestion.uniqueQuestionId, "1")
            body(answer.toJson())
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            validateAnswerResponse(
                questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q3")
            )
        }
    }

    @Test
    fun `given user with some label, when answering question for others with label condition for different label than user's, expect question skipped`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
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
            validateAnswerResponse(
                questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q3"),
                myself = false
            )
        }
    }

    @Test
    fun `given user with some label, when answering question with label condition for same label as user's, expect question shown`() {
        val label = getOrCreateLabel("NONI")
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
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
            validateAnswerResponse(questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2"))
        }
    }

    @Test
    fun `given answer for KEELK9A with type DESCRIPTIVE_WITH_PHOTO, expect KEELK6 returned`() {
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
            val answer = ImagesAnswer(keelk9aQuestion.uniqueQuestionId, "22", listOf())
            body(jacksonObjectMapper().writeValueAsString(answer))
            post("v1/triage/answers")
        } Then {
            val keelk6Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(keelk6Question, false)
        }
    }

    @Test
    fun `given high urgency answers to KEELK1, expect next question and action go to chat`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val multipleChoiceAnswer = MultipleChoiceAnswer(keelk1Question.uniqueQuestionId, setOf(1, 2))
            body(jacksonObjectMapper().writeValueAsString(multipleChoiceAnswer))
            post("v1/triage/answers?supportsContinuation=true")
        } Then {
            body("action", equalTo(AnswerActionResponse.ASK_FOR_CHAT.toString()))
            validateAnswerResponse(nextQuestionToAsk(keelk1Question, triageStatus), false)
        }
    }

    @Test
    fun `given medarea question, when answering age question with type NUMBER, expect next question returned`() {
        val label = getOrCreateLabel("NONI")
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        saveMedicalAreaAnswer(triageStatus.id, TEST_QUESTIONNAIRE)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val stringAnswer = StringAnswer("q1", "22")
            body(jacksonObjectMapper().writeValueAsString(stringAnswer))
            post("v1/triage/answers")
        } Then {
            body("action", equalTo("NEXT"))
            val nextQuestion = questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2")
            validateAnswerResponse(nextQuestion)
        }
    }

    @Test
    fun `given answer to PRO1 for myself, when answering medical area as KEEL, expect KEELK1 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val singleChoiceAnswer = SingleChoiceAnswer(questionnaireModel.medicalAreaQuestion.uniqueQuestionId, 21)
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(keelk1Question)
        }
    }

    @Test
    fun `given missing answer for PRO1, when trying to answer question next to PRO1, expect 400 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID), triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val pro2Question =
                questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, "TRIAGEOTHER_AUTHORIZED")
            val singleChoiceAnswer = SingleChoiceAnswer(pro2Question.uniqueQuestionId, 1)
            body(jacksonObjectMapper().writeValueAsString(singleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given answer for PRO1 and medical area question only, when trying to answer KEELK4, expect 400 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val keelk4Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
            val multipleChoiceAnswer = MultipleChoiceAnswer(keelk4Question.uniqueQuestionId, setOf(1, 2, 3))
            body(jacksonObjectMapper().writeValueAsString(multipleChoiceAnswer))
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given answer for question that preceeds TRIAGEOTHER_BIRTHDATE, when answering TRIAGEOTHER_BIRTHDATE with invalid age, expect 400 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveAnswer(
            questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, "TRIAGEOTHER_GENDER"),
            triageStatus.id,
            1
        )
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val dateAnswer =
                DateAnswer(questionnaireModel.birthdayProfileQuestion.uniqueQuestionId, LocalDate.of(1455, 1, 1))
            body(dateAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given answer for question that preceeds TRIAGEOTHER_BIRTHDATE, when answering TRIAGEOTHER_BIRTHDATE with valid age, expect next question returned`() {
        val label = getOrCreateLabel("NONI")
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveAnswer(
            questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, "TRIAGEOTHER_GENDER"),
            triageStatus.id,
            1
        )
        Given {
            auth().oauth2(accessToken(user))
        } When {
            val dateAnswer =
                DateAnswer(questionnaireModel.birthdayProfileQuestion.uniqueQuestionId, LocalDate.of(2010, 1, 1))
            body(dateAnswer.toJson())
            post("v1/triage/answers")
        } Then {
            val nextQuestion = questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, "TRIAGEOTHER_BSN")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextQuestion)
        }
    }

    @Test
    fun `when answering Uncertain answer for VROUW2, expect VROUW3 returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)

        saveMedicalAreaAnswer(triageStatus.id, "VROUW")

        val alg4Question = questionnaireModel.getQuestionByUniqueId("VROUW", "ALG4") // Zwanger
        saveAnswer(alg4Question, triageStatus.id, 2)
        val vrouw1Question = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW1")
        saveAnswer(vrouw1Question.uniqueQuestionId, triageStatus.id, MultipleChoiceAnswer(vrouw1Question.uniqueQuestionId, setOf(8))) // Welk van deze klachten
        val alg7Question = questionnaireModel.getQuestionByUniqueId("VROUW", "ALG7") // Koorts?
        saveAnswer(alg7Question, triageStatus.id, 2)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val question = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW2")
            val answer = UncertainAnswer(question.uniqueQuestionId)
            body(jacksonObjectMapper().writeValueAsString(answer))
            post("v1/triage/answers")
        } Then {
            val nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW3")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextQuestion)
        }
    }

    @Test
    fun `when answering Uncertain answer for HARTK3A, expect HART4 to be returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)

        saveMedicalAreaAnswer(triageStatus.id, "HARTK")

        val hartk1Question = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK1")

        saveAnswer(hartk1Question.uniqueQuestionId, triageStatus.id, MultipleChoiceAnswer(hartk1Question.uniqueQuestionId, setOf(9)))
        val hartk2Question = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK2")
        saveAnswer(hartk2Question.uniqueQuestionId, triageStatus.id, MultipleChoiceAnswer(hartk2Question.uniqueQuestionId, setOf(10)))
        val alg7Question = questionnaireModel.getQuestionByUniqueId("HARTK", "ALG7")
        saveAnswer(alg7Question, triageStatus.id, 2)
        val hart3Question = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK3")
        saveAnswer(hart3Question.uniqueQuestionId, triageStatus.id, MultipleChoiceAnswer(hart3Question.uniqueQuestionId, setOf(1,10)))

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val question = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK3A")
            val answer = UncertainAnswer(question.uniqueQuestionId)
            body(jacksonObjectMapper().writeValueAsString(answer))
            post("v1/triage/answers")
        } Then {
            val nextQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK4")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextQuestion)
        }
    }

    // Multiselect Tests
    @Test
    fun `given a multiselect question auto return the correct question order`() {
        createMultiSelectQuestionnaire()

        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaAnswer(triageStatus.id, MULTI_SELECT_QUESTIONNAIRE)

        val question1 = questionnaireModel.findFirstQuestion(MULTI_SELECT_QUESTIONNAIRE)
        saveMultiSelectionAnswer(question1, triageStatus.id, setOf(1, 3))
        saveMultiSelectionAnswer(
            questionnaireModel.getQuestionByUniqueId(MULTI_SELECT_QUESTIONNAIRE, "q2"),
            triageStatus.id, setOf(2)
        )

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val answer =
                BooleanAnswer("q5", 1)
            body(answer.toJson())
            post("v1/triage/answers")
        } Then {
            val nextQuestion = questionnaireModel.getQuestionByUniqueId(MULTI_SELECT_QUESTIONNAIRE, "q3")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextQuestion)
        }
    }

    @Test
    fun `When going to the questionnaire "Geslachsorganen Vrouw" expext all the subpaths to be handled correctly`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaAnswer(triageStatus.id, "VROUW")

        var nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "ALG4")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW1")
        saveAnswer(nextQuestion, triageStatus.id, 1)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "ALG7")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW2")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(1,5,6))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "PIJ1")
        saveAnswer(nextQuestion, triageStatus.id, 8)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW2A")
        saveAnswer(nextQuestion, triageStatus.id, 8)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW2B")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW3")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(1,2))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW3A")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(2,3))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW3B")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "ALG13")
        saveAnswer(nextQuestion, triageStatus.id, 6)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "ALG13A")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW5")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(1,2))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "ALG19")
        saveAnswer(nextQuestion, triageStatus.id, 1)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val answer =
                SingleChoiceAnswer("ALG19A", 2)
            body(answer.toJson())
            post("v1/triage/answers")
        } Then {
            val nextAskedQuestion = questionnaireModel.getQuestionByUniqueId("VROUW", "VROUW6")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextAskedQuestion)
        }
    }

    @Test
    fun `When going to the questionnaire "Hartkloppingen" expext all the subpaths to be handled correctly`() {
        val label = createLabel()
        val user = createCustomer(label, "c1", Gender.MALE)
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaAnswer(triageStatus.id, "HARTK")

        var nextQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK1")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(1, 9))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK2")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(6, 9))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "ALG7")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK3")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(2, 4, 10))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK3A")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK4")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(2, 4))

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val answer =
                SingleChoiceAnswer("ALG13", 3)
            body(answer.toJson())
            post("v1/triage/answers")
        } Then {
            val nextAskedQuestion = questionnaireModel.getQuestionByUniqueId("HARTK", "HARTK5")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextAskedQuestion)
        }
    }

    @Test
    fun `When answering "don't know" for multiple subpaths in a MPC question, handle it correctly`() {
        val label = createLabel()
        val user = createCustomer(label, "c1", Gender.FEMALE)
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaAnswer(triageStatus.id, "ALLER")

        var nextQuestion = questionnaireModel.getQuestionByUniqueId("ALLER", "ALLER1")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(10))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("ALLER", "ALG7")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("ALLER", "ALLER2")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(4))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("ALLER", "ALLER2A")
        saveAnswer(nextQuestion.uniqueQuestionId, triageStatus.id, UncertainAnswer(nextQuestion.uniqueQuestionId))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("ALLER", "ALLER3")
        saveMultiSelectionAnswer(nextQuestion, triageStatus.id, setOf(2,3))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("ALLER", "ALLER3A")
        saveAnswer(nextQuestion.uniqueQuestionId, triageStatus.id, UncertainAnswer(nextQuestion.uniqueQuestionId))

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val answer =
                UncertainAnswer("ALLER6")
            body(answer.toJson())
            post("v1/triage/answers")
        } Then {
            val nextAskedQuestion = questionnaireModel.getQuestionByUniqueId("ALLER", "ALG13")
            body("action", equalTo("NEXT"))
            validateAnswerResponse(nextAskedQuestion)
        }
    }

    @Test
    fun `given mpc question with subquestion that is also a mpc and the answer to the subquestion is high urgent`() {

        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)

        saveMedicalAreaAnswer(triageStatus.id, "ARMKL")

        val arm1Question = questionnaireModel.getQuestionByUniqueId("ARMKL", "ARMKL1")
        saveAnswer(arm1Question.uniqueQuestionId, triageStatus.id, MultipleChoiceAnswer(arm1Question.uniqueQuestionId, setOf(5)))

        Given {
            auth().oauth2(accessToken(user))
        } When {
            val question = questionnaireModel.getQuestionByUniqueId("ARMKL", "ARMKL8")
            val answer = MultipleChoiceAnswer(question.uniqueQuestionId, setOf(1))
            body(jacksonObjectMapper().writeValueAsString(answer))
            post("v1/triage/answers?supportsContinuation=true")
        } Then {
            body("action", equalTo(AnswerActionResponse.ASK_FOR_CHAT.toString()))
        }
    }
}
