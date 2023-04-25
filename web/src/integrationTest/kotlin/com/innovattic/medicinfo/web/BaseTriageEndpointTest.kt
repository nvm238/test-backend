package com.innovattic.medicinfo.web

import com.innovattic.medicinfo.dbschema.tables.pojos.TriageAnswer
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import com.innovattic.medicinfo.logic.dto.triage.AnswerRequest
import com.innovattic.medicinfo.logic.dto.triage.DateAnswer
import com.innovattic.medicinfo.logic.dto.triage.MultipleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.SingleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.StringAnswer
import com.innovattic.medicinfo.logic.triage.model.Action
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.Answer
import com.innovattic.medicinfo.logic.triage.model.BIRTHDAY_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.ModelMapper
import com.innovattic.medicinfo.logic.triage.model.OTHER_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.QuestionDefinition
import com.innovattic.medicinfo.logic.triage.model.QuestionType
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireModel
import com.innovattic.medicinfo.logic.triage.model.external.Condition
import io.restassured.response.ValidatableResponse
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeAll
import java.time.LocalDate
import java.time.LocalDateTime

abstract class BaseTriageEndpointTest : BaseEndpointTest() {

    companion object {
        const val SCHEMA_VERSION = 0

        // A test questionnaire, which can be filled with a testcase-specific questionnaire
        // Note that this must be defined as questionnaire in the test json as well, because at initialization time,
        // the ModelMapper uses that list of questionnaires to create the possible answers for the
        // 'medical area question'.
        const val TEST_QUESTIONNAIRE = "test"
    }

    lateinit var questionnaireModel: QuestionnaireModel

    @BeforeAll
    open fun readSchema() {
        questionSchemaService.loadFile(SCHEMA_VERSION, "triage-test-model.json")
        questionnaireModel = questionSchemaService.getQuestionSchema(SCHEMA_VERSION)!!
    }

    @BeforeAll
    open fun setTimeOfDay() {
        // Most testcases expect to run during 'working hours'
        // don't make the testresults dependent on the time they are executed
        clock.setTime(LocalDateTime.of(2022, 3, 17, 13, 0, 0))
    }

    fun saveAnswer(
        questionDefinition: QuestionDefinition,
        triageStatusId: Int,
        answer: Int
    ): TriageAnswer = saveAnswer(
        questionDefinition.uniqueQuestionId,
        triageStatusId,
        SingleChoiceAnswer(questionDefinition.uniqueQuestionId, answer)
    )

    fun saveMultiSelectionAnswer(
        questionDefinition: QuestionDefinition,
        triageStatusId: Int,
        answer: Set<Int>
    ): TriageAnswer = saveAnswer(
        questionDefinition.uniqueQuestionId,
        triageStatusId,
        MultipleChoiceAnswer(questionDefinition.uniqueQuestionId, answer)
    )

    fun saveAnswer(
        questionDefinition: QuestionDefinition,
        triageStatusId: Int,
        answer: String
    ): TriageAnswer = saveAnswer(
        questionDefinition.uniqueQuestionId, triageStatusId, StringAnswer(questionDefinition.uniqueQuestionId, answer)
    )

    fun saveAnswer(
        uniqueQuestionId: String,
        triageStatusId: Int,
        answer: AnswerRequest
    ): TriageAnswer = triageAnswerDao.saveNew(
        triageStatusId,
        uniqueQuestionId,
        answer.toDomain().toJson(),
        answer.toJson()
    )

    fun saveMedicalAreaKEELKAnswer(triageStatusId: Int): TriageAnswer = saveMedicalAreaAnswer(triageStatusId, "KEELK")

    fun saveMedicalAreaOVERIGAnswer(triageStatusId: Int): TriageAnswer =
        saveMedicalAreaAnswer(triageStatusId, OTHER_QUESTIONNAIRE_ID)

    fun saveMedicalAreaAnswer(triageStatusId: Int, medicalArea: String): TriageAnswer =
        saveAnswer(
            questionnaireModel.medicalAreaQuestion, triageStatusId,
            questionnaireModel.medicalAreaQuestion.answer.find { it.action.actionText == medicalArea }?.id
                ?: throw IllegalStateException("No questionnaire name $medicalArea")
        )

    fun answerProfileQuestionsWithoutMedicalArea(triageStatusId: Int) {
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatusId, 2)
        saveAnswer(questionnaireModel.genderProfileQuestion, triageStatusId, 1)
        saveAnswer(
            questionnaireModel.birthdayProfileQuestion.uniqueQuestionId,
            triageStatusId,
            DateAnswer(BIRTHDAY_QUESTION_UNIQUE_ID, LocalDate.of(2000, 4, 10))
        )
        val questionBeforeMedarea = questionnaireModel.getQuestionnaire(PROFILE_QUESTIONNAIRE_ID)
            ?.questions
            ?.last { q -> q.uniqueQuestionId != questionnaireModel.medicalAreaQuestion.uniqueQuestionId }
            ?: throw IllegalStateException("Profile questionnaire does not exist!")
        saveAnswer(questionBeforeMedarea, triageStatusId, "123456789")
    }

    fun nextQuestionToAsk(questionDefinition: QuestionDefinition, triageStatus: TriageStatus): QuestionDefinition {
        val userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)
        return questionnaireModel.getNextAskableQuestion(questionDefinition, userProfile)!!
    }

    val answerQuestionObjectName = "nextQuestion"
    val startTriageQuestionObjectName = "question"

    fun ValidatableResponse.validateAnswerResponse(
        questionDefinition: QuestionDefinition,
        myself: Boolean = true,
    ) {
        validateQuestionResponse(questionDefinition, myself, answerQuestionObjectName)
        validateAllPossibleAnswers(questionDefinition.answer, answerQuestionObjectName)
    }

    fun ValidatableResponse.validateStartTriageResponse(
        questionDefinition: QuestionDefinition,
        myself: Boolean = true,
    ) {
        validateQuestionResponse(questionDefinition, myself, startTriageQuestionObjectName)
        validateAllPossibleAnswers(questionDefinition.answer, startTriageQuestionObjectName)
    }

    fun ValidatableResponse.validateQuestionResponse(
        questionDefinition: QuestionDefinition,
        myself: Boolean = true,
        questionObjectName: String = "nextQuestion"
    ) {
        body("$questionObjectName.questionId", CoreMatchers.equalTo(questionDefinition.uniqueQuestionId))
        body(
            "$questionObjectName.title",
            CoreMatchers.equalTo(if (myself) questionDefinition.question else questionDefinition.questionForCaregiver)
        )
        body("$questionObjectName.isRequired", CoreMatchers.equalTo(questionDefinition.isQuestionRequired))
        body("$questionObjectName.isUncertainAllowed", CoreMatchers.equalTo(questionDefinition.isDontKnow))
    }

    private fun ValidatableResponse.validateAllPossibleAnswers(
        answers: List<Answer>,
        questionObjectName: String = "question"
    ) {
        body("$questionObjectName.answers.size()", CoreMatchers.`is`(answers.size))
        answers
            .forEachIndexed { idx, value -> validateAnswer(idx, value, questionObjectName) }
    }

    fun ValidatableResponse.validateAnswer(idx: Int, answer: Answer, questionObjectName: String) {
        body("$questionObjectName.answers[$idx].id", CoreMatchers.equalTo(answer.id))
        body("$questionObjectName.answers[$idx].answerText", CoreMatchers.equalTo(answer.answerText))
        body(
            "$questionObjectName.answers[$idx].action",
            CoreMatchers.equalTo(answer.action.actionType.toString())
        )
    }

    fun ValidatableResponse.vaildateFemaleMedareaAnswers(myself: Boolean, questionObjectName: String) {
        validateQuestionResponse(questionnaireModel.medicalAreaQuestion, myself, questionObjectName = questionObjectName)
        val medareaAnswers = questionnaireModel.medicalAreaQuestion.answer
        validateAnswer(
            0,
            medareaAnswers.find { it.id == 1 } ?: throw IllegalStateException("No answer"),
            questionObjectName)
        validateAnswer(
            22,
            medareaAnswers.find { it.id == 24 } ?: throw IllegalStateException("No answer"),
            questionObjectName)
        validateAnswer(
            23,
            medareaAnswers.find { it.id == 25 } ?: throw IllegalStateException("No answer"),
            questionObjectName)
    }

    fun ValidatableResponse.validatePrefilledAnswerRequest(firstAreaQuestion: QuestionDefinition, answer: Any) {
        body("nextAnswer.questionId", CoreMatchers.equalTo(firstAreaQuestion.uniqueQuestionId))
        body("nextAnswer.type", CoreMatchers.equalTo(firstAreaQuestion.questionType.name.lowercase()))
        body("nextAnswer.answer", CoreMatchers.equalTo(answer))
    }

    protected fun createBooleanQuestion(
        id: String,
        pos: Int,
        action: ActionType = ActionType.GO_TO_QUESTION,
        urgency: Int = 0,
        nextQuestion: QuestionDefinition?,
        questionnaireID: String = TEST_QUESTIONNAIRE,
    ) =
        QuestionDefinition(
            id,
            questionnaireID,
            pos,
            "test",
            "Boolean question $id",
            "Boolean question for caregiver  $id",
            id,
            "",
            "",
            "",
            QuestionType.BOOLEAN,
            true,
            false,
            listOf(),
            listOf(
                Answer(
                    1, "Ja", false, Action(
                        actionType = action,
                        urgency = urgency,
                        urgencyName = "U$urgency",
                        actionText = nextQuestion?.uniqueQuestionId ?: "",
                        nextQuestion = nextQuestion
                    )
                ),
                Answer(
                    1, "Nee", false, Action(
                        actionType = action,
                        urgency = 0,
                        urgencyName = "U$urgency",
                        actionText = nextQuestion?.uniqueQuestionId ?: "",
                        nextQuestion = nextQuestion
                    )
                )
            )
        )

    protected fun createNumberQuestion(
        id: String,
        pos: Int,
        action: ActionType = ActionType.GO_TO_QUESTION,
        urgency: Int = 0,
        conditions: List<Condition> = listOf(),
        answerVisibilityConditions: List<Condition> = listOf(),
        nextQuestion: QuestionDefinition?,
        questionnaireID: String = TEST_QUESTIONNAIRE
    ) =
        QuestionDefinition(
            id,
            questionnaireID,
            pos,
            "test",
            "Number question $id",
            "Number question for caregiver $id",
            id,
            "",
            "",
            "",
            QuestionType.NUMBER,
            true,
            false,
            conditions.map { ModelMapper.parsePredicate(it) },
            listOf(
                Answer(
                    1, "", false, Action(
                        actionType = action,
                        urgency = urgency,
                        urgencyName = "U$urgency",
                        actionText = nextQuestion?.uniqueQuestionId ?: "",
                        nextQuestion = nextQuestion
                    ),
                    answerVisibilityConditions.map { ModelMapper.parsePredicate(it) }
                )
            )
        )

    protected fun createMultiSelectionQuestion(
        id: String,
        pos: Int,
        questionnaireID: String = TEST_QUESTIONNAIRE,
        answers: List<Answer>,
        nextMainAction: Action,
    )  =
        QuestionDefinition(
            id,
            questionnaireID,
            pos,
            "test",
            "Multi select question $id",
            "Multi select question for caregivers",
            id,
            "",
            "",
            "",
            QuestionType.MULTI_SELECTION,
            true,
            false,
            listOf(),
            answers,
            nextMainAction,
        )

    protected fun createQuestion(
        id: String,
        pos: Int,
        isRequired: Boolean,
        isDontKnow: Boolean,
        questionType: QuestionType,
        answers: List<Answer>,
        conditions: List<Condition> = listOf(),
        questionnaireID: String = TEST_QUESTIONNAIRE,
    ) =
        QuestionDefinition(
            id,
            questionnaireID,
            pos,
            "test",
            "test question $id",
            "test for caregiver question $id",
            id,
            "",
            "",
            "",
            questionType,
            isRequired,
            isDontKnow,
            conditions.map { ModelMapper.parsePredicate(it) },
            answers
        )
}
