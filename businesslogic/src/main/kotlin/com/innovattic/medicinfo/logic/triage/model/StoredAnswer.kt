package com.innovattic.medicinfo.logic.triage.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate

private val storedAnswerMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)

/**
 * Class that is a database representation of an Answer to the questionnaire question
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SingleChoice::class, name = "single_selection"),
    JsonSubTypes.Type(value = MultipleChoice::class, name = "multi_selection"),
    JsonSubTypes.Type(value = Descriptive::class, name = "descriptive"),
    JsonSubTypes.Type(value = Skip::class, name = "skip"),
    JsonSubTypes.Type(value = Uncertain::class, name = "uncertain"),
    JsonSubTypes.Type(value = DescriptiveWithImages::class, name = "descriptive_with_images"),
    JsonSubTypes.Type(value = Date::class, name = "date")
)
sealed class StoredAnswer(
    open val questionId: String
) {
    companion object {
        fun ofJson(json: String): StoredAnswer = storedAnswerMapper.readValue(json, StoredAnswer::class.java)
    }

    fun toJson(): String = storedAnswerMapper.writeValueAsString(this)

    /**
     * This method resolves the stored answer to actual answer text, ex. answer ids to text
     *
     * @param questionDefinition object containing question data
     * @return list of ResolvedAnswer objects
     *
     * @see QuestionDefinition
     */
    abstract fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer>
}

data class SingleChoice(override val questionId: String, val answer: Int) : StoredAnswer(questionId) {
    override fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer> {
        val answerDefinition = questionDefinition.getAnswerById(answer)

        return listOf(
            ResolvedAnswer(
                answerDefinition.answerText,
                answerDefinition.action.urgency,
                answerDefinition.action.urgencyName,
                answerDefinition.isDivergent
            )
        )
    }
}

data class MultipleChoice(override val questionId: String, val answer: Set<Int>) : StoredAnswer(questionId) {
    override fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer> {
        return answer.map { questionDefinition.getAnswerById(it) }
            .map { ResolvedAnswer(it.answerText, it.action.urgency, it.action.urgencyName, it.isDivergent) }
    }
}

data class Descriptive(override val questionId: String, val answer: String) : StoredAnswer(questionId) {
    override fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer> {
        val urgency = questionDefinition.answer[0].action.urgency
        val urgencyName = questionDefinition.answer[0].action.urgencyName
        val isDivergent = questionDefinition.answer[0].isDivergent

        return listOf(ResolvedAnswer(answer, urgency, urgencyName, isDivergent))
    }
}

data class DescriptiveWithImages(
    override val questionId: String,
    val description: String,
    val imageIds: List<String>
) : StoredAnswer(questionId) {
    override fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer> {
        val urgency = questionDefinition.answer[0].action.urgency
        val urgencyName = questionDefinition.answer[0].action.urgencyName
        val isDivergent = questionDefinition.answer[0].isDivergent
        // business requirement is to append text to the description indicating that there are images present
        val desc = if (imageIds.isEmpty()) description else "$description >> zie ${imageIds.size} bijlage(s)"
        return listOf(ResolvedAnswer(desc, urgency, urgencyName, isDivergent))
    }
}

data class Skip(override val questionId: String) : StoredAnswer(questionId) {
    override fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer> {
        return listOf(ResolvedAnswer("overgeslagen", -1, null, false))
    }
}

data class Uncertain(override val questionId: String) : StoredAnswer(questionId) {
    override fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer> {
        return listOf(ResolvedAnswer("weet ik niet", -1, null, false))
    }
}

data class Date(override val questionId: String, val answer: LocalDate) : StoredAnswer(questionId) {
    override fun resolveAnswers(questionDefinition: QuestionDefinition): List<ResolvedAnswer> {
        val urgency = questionDefinition.answer[0].action.urgency
        val urgencyName = questionDefinition.answer[0].action.urgencyName
        val isDivergent = questionDefinition.answer[0].isDivergent

        return listOf(ResolvedAnswer(answer.toString(), urgency, urgencyName, isDivergent))
    }
}

/**
 * This object represents answer that is resolved to human-readable form. It is meant to hold text values of answers,
 * their urgency and divergent flag. Those values can be extracted from question schema model
 */
data class ResolvedAnswer(
    val answerText: String,
    val urgency: Int,
    val urgencyName: String?,
    val isDivergent: Boolean
)
