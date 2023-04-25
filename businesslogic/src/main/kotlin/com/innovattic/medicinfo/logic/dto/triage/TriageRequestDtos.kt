package com.innovattic.medicinfo.logic.dto.triage

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.medicinfo.logic.triage.model.Date
import com.innovattic.medicinfo.logic.triage.model.Descriptive
import com.innovattic.medicinfo.logic.triage.model.DescriptiveWithImages
import com.innovattic.medicinfo.logic.triage.model.MultipleChoice
import com.innovattic.medicinfo.logic.triage.model.SingleChoice
import com.innovattic.medicinfo.logic.triage.model.Skip
import com.innovattic.medicinfo.logic.triage.model.StoredAnswer
import com.innovattic.medicinfo.logic.triage.model.Uncertain
import java.time.LocalDate

data class StopTriageRequest(
    val wantsChat: Boolean,
    val reason: String?,
    val stopReason: StopReason = StopReason.UNKNOWN
)

enum class StopReason {
    /**
     * user wants to stop a triage in any point and does NOT want to chat
     */
    UNKNOWN,

    /**
     * user answered a question with high urgency answer and decided not to continue, but go to chat
     */
    HIGH_URGENCY_CHAT,

    /**
     * user wants to stop a triage in any point and wants to chat with the employee
     */
    WANTS_CHAT,

    /**
     * DEPRECATED: USE OTHER_FINISHED
     * user stopped the chat on medical area question (most probably because has trouble choosing the right one) and wants to chat
     */
    NO_MEDICAL_AREA,

    /**
     * User finished the OVERIGE questionnaire, this is the new way of handling NO_MEDICAL_AREA.
     */
    OTHER_FINISHED,
}

private val answerRequestMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)

/**
 * Class that is a web(client request) representation of an Answer to the questionnaire question.
 * For every question type we need a different subtype, so types are not remapped to different types
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SingleChoiceAnswer::class, name = "single_selection"),
    JsonSubTypes.Type(value = MultipleChoiceAnswer::class, name = "multi_selection"),
    JsonSubTypes.Type(value = StringAnswer::class, name = "descriptive"),
    JsonSubTypes.Type(value = SkipAnswer::class, name = "skip"),
    JsonSubTypes.Type(value = UncertainAnswer::class, name = "uncertain"),
    JsonSubTypes.Type(value = BooleanAnswer::class, name = "boolean"),
    JsonSubTypes.Type(value = SliderAnswer::class, name = "slider"),
    JsonSubTypes.Type(value = ImagesAnswer::class, name = "images"),
    JsonSubTypes.Type(value = DateAnswer::class, name = "date")
)
sealed class AnswerRequest(
    open val questionId: String
) {
    companion object {
        fun ofJson(json: String): AnswerRequest = answerRequestMapper.readValue(json, AnswerRequest::class.java)
    }

    fun toJson(): String = answerRequestMapper.writeValueAsString(this)

    fun toDomain(): StoredAnswer {
        return when (this) {
            is SingleChoiceAnswer -> {
                SingleChoice(this.questionId, this.answer)
            }
            is BooleanAnswer -> {
                SingleChoice(this.questionId, this.answer)
            }
            is SliderAnswer -> {
                SingleChoice(this.questionId, this.answer)
            }
            is MultipleChoiceAnswer -> {
                MultipleChoice(this.questionId, this.answer)
            }
            is StringAnswer -> {
                Descriptive(this.questionId, this.answer)
            }
            is DateAnswer -> {
                Date(this.questionId, this.answer)
            }
            is SkipAnswer -> {
                Skip(this.questionId)
            }
            is UncertainAnswer -> {
                Uncertain(this.questionId)
            }
            is ImagesAnswer -> {
                DescriptiveWithImages(this.questionId, this.description, this.imageIds)
            }
        }
    }
}

data class SingleChoiceAnswer(override val questionId: String, val answer: Int) : AnswerRequest(questionId)
data class BooleanAnswer(override val questionId: String, val answer: Int) : AnswerRequest(questionId)
data class SliderAnswer(override val questionId: String, val answer: Int) : AnswerRequest(questionId)
data class MultipleChoiceAnswer(override val questionId: String, val answer: Set<Int>) : AnswerRequest(questionId)
data class StringAnswer(override val questionId: String, val answer: String) : AnswerRequest(questionId)
data class DateAnswer(override val questionId: String, val answer: LocalDate) : AnswerRequest(questionId)
data class SkipAnswer(override val questionId: String) : AnswerRequest(questionId)
data class UncertainAnswer(override val questionId: String) : AnswerRequest(questionId)
data class ImagesAnswer(
    override val questionId: String,
    val description: String,
    val imageIds: List<String>
) : AnswerRequest(questionId)
