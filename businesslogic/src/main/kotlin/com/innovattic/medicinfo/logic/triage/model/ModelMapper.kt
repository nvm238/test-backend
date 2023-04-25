package com.innovattic.medicinfo.logic.triage.model

import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.logic.triage.model.external.Condition
import com.innovattic.medicinfo.logic.triage.model.external.ConditionType
import com.innovattic.medicinfo.logic.triage.model.external.ContainsValueCondition
import com.innovattic.medicinfo.logic.triage.model.external.SingleValueCondition

object ModelMapper {

    fun mapExternalToInternal(
        externalSchema: com.innovattic.medicinfo.logic.triage.model.external.QuestionnaireModel,
        version: Int
    ): QuestionnaireModel {
        val questionnaireMap = mutableMapOf<String, QuestionnaireDefinition>()

        externalSchema.questionnaires.forEach { questionnaire ->
            val tempQuestionPool = mutableMapOf<String, QuestionDefinition>()
            // GO_TO_QUESTION always jumps forward. We are reversing the questionnaire to look back on already initialized
            // questions, so we can assign instances to the nextQuestion's answer's action property. Instances are stored
            // in the pool and are extracted when GO_TO_QUESTION point to its ID
            val questionDefinitions = questionnaire.questions.reversed().mapIndexed { idx, questionDefinition ->
                val finalDefinition = QuestionDefinition(
                    uniqueQuestionId = questionDefinition.uniqueQuestionNumber,
                    questionnaireId = questionnaire.id,
                    position = questionnaire.questions.size - 1 - idx, // size is 1-based, idx is zero-based, so subtract 1 extra
                    questionCategory = questionDefinition.questionCategory,
                    question = questionDefinition.question,
                    questionForCaregiver = questionDefinition.questionForCaregiver,
                    shortQuestionVpk = questionDefinition.shortQuestionVpk,
                    additionalInfo = questionDefinition.additionalInfo,
                    pictureWithQuestion = questionDefinition.pictureWithQuestion,
                    urgency = questionDefinition.urgency,
                    questionType = QuestionType.valueOf(questionDefinition.questionType.toString()),
                    isQuestionRequired = questionDefinition.isQuestionRequired,
                    isDontKnow = questionDefinition.isDontKnow,
                    conditions = questionDefinition.conditions.map { parsePredicate(it) },
                    nextMainAction = questionDefinition.nextMainAction?.let {
                        parseAction(it, tempQuestionPool)
                    },
                    answer = questionDefinition.answer.map {
                        Answer(
                            id = it.id,
                            answerText = it.answerText,
                            isDivergent = it.isDivergent,
                            action = parseAction(it.action, tempQuestionPool),
                            visibilityConditions = it.visibilityConditions.map { vc -> parsePredicate(vc) },
                            alternativeNames = it.alternativeNames
                        )
                    }
                )
                tempQuestionPool[finalDefinition.uniqueQuestionId] = finalDefinition
                finalDefinition
            }
            questionnaireMap[questionnaire.id] =
                QuestionnaireDefinition(
                    questionnaire.id,
                    questionnaire.name,
                    // reverse initialized QuestionDefinition list to bring back initial order
                    questionDefinitions.reversed()
                )
        }

        return QuestionnaireModel(version, questionnaireMap)
    }

    private fun parseAction(
        action: com.innovattic.medicinfo.logic.triage.model.external.Action,
        tempQuestionPool: MutableMap<String, QuestionDefinition>
    ) = Action(
        actionType = action.actionType.toInternal(),
        // new json format uses null urgency; backend code still uses -1 to indicate 'no urgency'
        urgency = action.urgency ?: -1,
        urgencyName = action.urgencyName,
        actionText = action.actionText,
        nextQuestion = tempQuestionPool[action.actionText]
    )

    private fun com.innovattic.medicinfo.logic.triage.model.external.ActionType.toInternal(): ActionType {
        return ActionType.valueOf(this.toString())
    }

    fun parsePredicate(externalCondition: Condition): (UserProfile) -> Boolean {
        when (externalCondition.type) {
            ConditionType.SEX -> {
                externalCondition as SingleValueCondition
                return { user -> user.gender == Gender.fromValue(externalCondition.value) }
            }
            ConditionType.AGE -> {
                externalCondition as SingleValueCondition
                return { user -> compare(externalCondition.operator, user.age, externalCondition.value.toInt()) }
            }
            ConditionType.LABEL -> {
                externalCondition as ContainsValueCondition
                if (externalCondition.operator != "in") {
                    throw UnsupportedOperationException("Failed to parse operator '${externalCondition.operator}'")
                }
                return { user -> user.labelCode.lowercase() in externalCondition.value }
            }
        }
    }

    private fun compare(operator: String, value1: Int, value2: Int): Boolean {
        return when (operator) {
            "=" -> value1 == value2
            "<" -> value1 < value2
            ">" -> value1 > value2
            ">=" -> value1 >= value2
            "<=" -> value1 <= value2
            else -> throw UnsupportedOperationException("Failed to parse operator '$operator'")
        }
    }
}
