package com.innovattic.medicinfo.logic.triage.model

import com.innovattic.medicinfo.database.dto.Gender

const val PROFILE_QUESTIONNAIRE_ID = "PRO"
const val PROFILE_WHO_QUESTION_UNIQUE_ID = "PRO1"
const val RELATION_QUESTION_UNIQUE_ID = "TRIAGEOTHER_RELATION"
const val FIRSTNAME_QUESTION_UNIQUE_ID = "TRIAGEOTHER_FIRSTNAME"
const val LASTNAME_QUESTION_UNIQUE_ID = "TRIAGEOTHER_LASTNAME"
const val GENDER_QUESTION_UNIQUE_ID = "TRIAGEOTHER_GENDER"
const val BIRTHDAY_QUESTION_UNIQUE_ID = "TRIAGEOTHER_BIRTHDATE"
const val BSN_QUESTION_UNIQUE_ID = "TRIAGEOTHER_BSN"
const val MEDICAL_AREA_QUESTION_UNIQUE_ID = "C-MEDAREA"
const val ADDITIONAL_INFO_QUESTION_UNIQUE_ID = "ADDITIONALQ"
const val OTHER_QUESTIONNAIRE_ID = "OVERIG"
const val OTHER_ADDITIONAL_INFO_QUESTION_UNIQUE_ID = "OVERI-ADDITIONALQ"
const val QUESTION_WHO_ANSWER_ID_FOR_SELF = 1

data class QuestionnaireModel(
    val version: Int,
    private var questionnaires: Map<String, QuestionnaireDefinition>
) {

    val whoProfileQuestion: QuestionDefinition by lazy { getProfileQuestionByUniqueId(PROFILE_WHO_QUESTION_UNIQUE_ID) }
    val genderProfileQuestion: QuestionDefinition by lazy { getProfileQuestionByUniqueId(GENDER_QUESTION_UNIQUE_ID) }
    val birthdayProfileQuestion: QuestionDefinition by lazy { getProfileQuestionByUniqueId(BIRTHDAY_QUESTION_UNIQUE_ID) }

    /**
     * This is the last question from Profile(PRO) questionnaire that is autogenerated when model mapping is done.
     * It contains medical areas to choose from
     */
    val medicalAreaQuestion: QuestionDefinition by lazy {
        this.getProfileQuestionByUniqueId(MEDICAL_AREA_QUESTION_UNIQUE_ID)
    }

    private fun getProfileQuestionByUniqueId(uniqueId: String) =
        getQuestionInSetByUniqueId(PROFILE_QUESTIONNAIRE_ID, uniqueId)
            ?: throw IllegalArgumentException("No question in questionnaire=$PROFILE_QUESTIONNAIRE_ID with id=$uniqueId")

    fun getQuestionByUniqueId(questionnaireName: String, uniqueId: String): QuestionDefinition {
        return getQuestionInSetByUniqueId(PROFILE_QUESTIONNAIRE_ID, uniqueId)
            ?: return getQuestionInSetByUniqueId(OTHER_QUESTIONNAIRE_ID, uniqueId)
            ?: return getQuestionInSetByUniqueId(questionnaireName, uniqueId)
                ?: throw IllegalArgumentException("No question in questionnaire=$questionnaireName with id=$uniqueId")
    }

    private fun getQuestionInSetByUniqueId(questionnaireName: String, uniqueId: String): QuestionDefinition? {
        val questionnaire = questionnaires[questionnaireName]
            ?: throw IllegalArgumentException("No questionnaire with name=$questionnaireName")

        return questionnaire.questions.find { it.uniqueQuestionId == uniqueId }
    }

    fun findFirstQuestion(questionnaireName: String): QuestionDefinition =
        questionnaires[questionnaireName]?.questions?.first()
            ?: throw IllegalArgumentException("No questionnaire with name=$questionnaireName")

    fun findFirstQuestionToAsk(userProfile: UserProfile): QuestionDefinition {
        val questionnaire = questionnaires[userProfile.currentQuestionnaireName]
            ?: throw IllegalArgumentException("No questionnaire with name=${userProfile.currentQuestionnaireName}")
        return questionnaire.questions.find { it.canAsk(userProfile) }
            ?: error("This questionnaire has no questions that can be ask for userProfile=$userProfile")
    }

    fun findLastQuestion(questionnaireName: String): QuestionDefinition =
        questionnaires[questionnaireName]?.questions?.last()
            ?: throw IllegalArgumentException("No questionnaire with name=$questionnaireName")

    fun getQuestionsNames(questionnaireName: String): List<String> =
        questionnaires[questionnaireName]?.questions?.map { it.uniqueQuestionId }
            ?: error("No questionnaire found with name=$questionnaireName")

    private fun getQuestionDefinitionsForQuestionnaire(questionnaireName: String) =
        questionnaires[questionnaireName]
            ?: throw IllegalArgumentException("No questionnaire with name=$questionnaireName")

    fun calculateProgress(questionDefinition: QuestionDefinition): Float {
        val questionnaireSize =
            getQuestionDefinitionsForQuestionnaire(questionDefinition.questionnaireId).questions.size.toFloat()
        // Since the position is zero-based, we need to add 1 so it finishes at 100%.
        return (questionDefinition.position + 1) / questionnaireSize
    }

    fun addQuestionnaire(questionnaire: QuestionnaireDefinition) {
        questionnaires = questionnaires + mapOf(questionnaire.id to questionnaire)
    }

    fun getQuestionnaire(questionnaireId: String): QuestionnaireDefinition? {
        return questionnaires[questionnaireId]
    }

    fun getNextAskableQuestion(
        questionDefinition: QuestionDefinition,
        userProfile: UserProfile
    ): QuestionDefinition? {
        var nextQuestion = getSubsequentQuestion(questionDefinition) ?: return null
        while (!nextQuestion.canAsk(userProfile)) {
            /*
            In case a question can't be asked (the conditions of the questions are not matched) we need to pick
            the next question that is askable, because the same question different conditions (male / female
            for example) are listed sequentially in the sheet.
            Please note that this function will not be used in case a user has skipped the question, or answered
            "I don't know", because this will already be handled in the TriageService.
            */
            nextQuestion = getSubsequentQuestion(nextQuestion) ?: return null
        }
        return nextQuestion
    }

    private fun getSubsequentQuestion(
        questionDefinition: QuestionDefinition
    ): QuestionDefinition? {
        return questionnaires[questionDefinition.questionnaireId]?.questions?.getOrNull(questionDefinition.position + 1)
    }
}

fun getGenderFromAnswer(storedAnswer: StoredAnswer): Gender {
    if (storedAnswer !is SingleChoice) error("Gender answer has to be single choice")
    return when (storedAnswer.answer) {
        1 -> Gender.FEMALE
        2 -> Gender.MALE
        3 -> Gender.OTHER
        else -> error("Gender is not defined")
    }
}
