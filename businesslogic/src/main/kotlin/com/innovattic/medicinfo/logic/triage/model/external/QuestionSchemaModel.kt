package com.innovattic.medicinfo.logic.triage.model.external

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class QuestionnaireModel(
    val questionnaires: List<Questionnaire>
)

data class Questionnaire(
    val id: String,
    val name: String,
    val questions: List<QuestionDefinition>
)

data class QuestionDefinition(
    val uniqueQuestionNumber: String,
    val questionCategory: String,
    val question: String,
    val questionForCaregiver: String,
    val shortQuestionVpk: String,
    val additionalInfo: String,
    val pictureWithQuestion: String,
    val urgency: String,
    val questionType: QuestionType,
    @JsonProperty("questionRequired") val isQuestionRequired: Boolean,
    @JsonProperty("dontKnow") val isDontKnow: Boolean,
    val conditions: List<Condition>,
    val answer: List<Answer>,
    val nextMainAction: Action?,
)

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

@JsonDeserialize(using = ConditionDeserializer::class)
sealed class Condition(
    @JsonProperty("name") open val type: ConditionType,
    open val operator: String,
)

private val supportedSingleValueOperators = setOf("=", "<", ">", ">=", "<=", "in")
private val supportedContainsValueOperators = setOf("in")

data class SingleValueCondition(
    @JsonProperty("name") override val type: ConditionType,
    override val operator: String,
    val value: String,
) : Condition(type, operator) {
    init {
        if (operator !in supportedSingleValueOperators) error("Operator '$operator' not supported!")
    }
}

data class ContainsValueCondition(
    @JsonProperty("name") override val type: ConditionType,
    override val operator: String,
    val value: List<String>,
) : Condition(type, operator) {
    init {
        if (operator !in supportedContainsValueOperators) error("Operator '$operator' not supported!")
    }
}

enum class ConditionType {
    SEX,
    AGE,
    LABEL
}

data class Answer(
    val id: Int,
    val answerText: String,
    @JsonProperty("divergent") val isDivergent: Boolean,
    val action: Action,
    val visibilityConditions: List<Condition> = emptyList(),
    val alternativeNames: List<String> = emptyList()
)

data class Action(
    val actionType: ActionType,
    val urgency: Int?,
    val urgencyName: String?,
    val actionText: String,
)

enum class ActionType {
    NEXT,
    SKIP_NEXT,
    ASK_FOR_CHAT,
    FINISH,
    UNAUTHORIZED,
    GO_TO_QUESTION,
    GO_TO_QUESTIONNAIRE
}
