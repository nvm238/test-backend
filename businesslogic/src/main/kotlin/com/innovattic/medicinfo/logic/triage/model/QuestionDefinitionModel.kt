package com.innovattic.medicinfo.logic.triage.model

import com.innovattic.common.error.createResponseStatusException

data class QuestionDefinition(
    val uniqueQuestionId: String,
    val questionnaireId: String,
    val position: Int,
    val questionCategory: String,
    val question: String,
    val questionForCaregiver: String,
    val shortQuestionVpk: String,
    val additionalInfo: String,
    val pictureWithQuestion: String,
    val urgency: String,
    val questionType: QuestionType,
    var isQuestionRequired: Boolean,
    val isDontKnow: Boolean,
    val conditions: List<(UserProfile) -> Boolean>,
    val answer: List<Answer>,
    // Defines the next action to take when all the branches of a multiple choice answer have been traversed.
    val nextMainAction: Action? = null,
    val isTranslated: Boolean = false,
) {
    fun isProfileQuestion() = questionnaireId == PROFILE_QUESTIONNAIRE_ID
    fun isMedicalAreaQuestion() = uniqueQuestionId == MEDICAL_AREA_QUESTION_UNIQUE_ID

    fun canAsk(user: UserProfile): Boolean =
        conditions.fold(true) { acc, condition -> acc && condition.invoke(user) }

    fun getQuestionTextFor(userProfile: UserProfile): String =
        if (userProfile.isAppUser) question else questionForCaregiver

    fun getAnswerById(answerId: Int) = answer.find { it.id == answerId }
        ?: throw createResponseStatusException { "No answer with id=$answerId for questionId=$uniqueQuestionId" }

    fun getAnswersByIdIn(answersIds: Set<Int>) = answer.filter { it.id in answersIds }

    /*
    In case a question is not answered (it's skipped by the user),
    we need to pick a next question *without* having an answer. Normally, the answer points us to the next question.
    In some cases, it makes sense to just continue with the next question, but in some cases it doesn't - for example,
    if the next question is asking more details on the current question. We should make it explicit in the question
    model at some point, but for now, we jump to the *furthest* question in the questionnaire.
     */
    fun findFurthermostNextAction(): Action? {
        return nextMainAction
            ?: answer.maxBy { it.action.nextQuestion?.position ?: -1 }.action
    }
}

enum class QuestionType {
    MULTI_SELECTION,
    SINGLE_SELECTION,
    SLIDER,
    DESCRIPTIVE,
    SINGLE_LINE_DESCRIPTIVE,
    NUMBER,
    DESCRIPTIVE_WITH_PHOTO,
    INSTRUCTION,
    BOOLEAN,
    DATE
}

data class Answer(
    val id: Int,
    val answerText: String,
    val isDivergent: Boolean,
    val action: Action,
    val visibilityConditions: List<(UserProfile) -> Boolean> = emptyList(),
    val alternativeNames: List<String> = emptyList()
) {
    fun isApplicable(user: UserProfile): Boolean =
        visibilityConditions.fold(true) { acc, condition -> acc && condition.invoke(user) }
}

data class Action(
    val actionType: ActionType,
    // urgency value. Lower value is higher priority (ie. U1 is most urgency).
    // -1 if there is no urgency. could be refactored to use NULL instead.
    val urgency: Int,
    // null if there is no urgency
    val urgencyName: String?,
    val actionText: String,
    val nextQuestion: QuestionDefinition?
)

enum class ActionType {
    /**
     * Now deprecated action that instructs to go to subsequent question in the questionnaire
     */
    @Deprecated(message = "This is kept only for backwards compat", replaceWith = ReplaceWith("GO_TO_QUESTION"))
    NEXT,
    /**
     * Now deprecated action that instructs to go to skip subsequent question in the questionnaire
     */
    @Deprecated(message = "This is kept only for backwards compat", replaceWith = ReplaceWith("GO_TO_QUESTION"))
    SKIP_NEXT,

    /**
     * Action that indicates that questionnaire can be finished at this point. This action is used with high urgency,
     * users can decide if they want to continue or finish the triage and go to chat
     */
    ASK_FOR_CHAT,

    /**
     * Indicates that there is no more question and triage should be finished
     */
    FINISH,

    /**
     * Indicates that triage should be finished, because user has no authorization to answer questions on behalf of other people
     */
    UNAUTHORIZED,

    /**
     * Instructs which question should be shown next
     */
    GO_TO_QUESTION,

    /**
     * Instructs which questionnaire should be shown
     */
    GO_TO_QUESTIONNAIRE
}
