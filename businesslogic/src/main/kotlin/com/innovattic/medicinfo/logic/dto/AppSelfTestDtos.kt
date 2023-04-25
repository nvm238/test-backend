package com.innovattic.medicinfo.logic.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AppSelfTestDto(
    val introduction: AppSelfTestIntroductionDto,
    val problemAreaQuestion: AppSelfTestProblemAreasDto,
    val generalQuestions: List<AppSelfTestQuestionDto>,
    val personalAssistance: AppSelfTestQuestionDto,
    @Deprecated("To be removed in the future. Left only not to break the frontend")
    val generalAdviceForBarelyNotInsured: String,
    @Deprecated("To be removed in the future. Left only not to break the frontend")
    val generalAdviceForBarelyInsured: String,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AppSelfTestIntroductionDto(
    val text: String,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AppSelfTestProblemAreasDto(
    val title: String,
    val description: String,
    val problemAreas: List<AppSelfTestProblemAreaDto>,
    val maximumAreas: Int
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AppSelfTestProblemAreaDto(
    val code: String,
    val name: String,
    val title: String?,
    val questions: List<AppSelfTestQuestionDto>,
    val mirro: MirroDto
)

/**
 * @param link link to Mirro module
 * @param blogId ID taken from Mirro JSON configuration stored in the database.
 * Value of this property is extracted from the [link] and is a last part of the url(after slash).
 *
 * ex. https://account.mirro.nl/prepare/register/eab47c3f7b96da1e4d5dcc3069fd4c7e1c12315a5877018670117d5f8662712f/54
 * blogId in the link above is 54
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class MirroDto(
    val title: String? = null,
    val shortDescription: String? = null,
    val longDescription: String? = null,
    val link: String? = null,
    val blogId: Int? = null
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AppSelfTestQuestionDto(
    val code: String,
    val type: SelfTestQuestionType,
    val title: String,
    val description: String?,
    @Schema(description = "Only for `single_choice`")
    val values: List<AppSelfTestAnswerDto>? = null,
    @Schema(description = "Only for `slider`")
    val minimum: Int? = null,
    @Schema(description = "Only for `slider`")
    val maximum: Int? = null,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AppSelfTestAnswerDto(
    val code: String,
    val title: String,
)

data class SelfTestAnswersDto(
    @field:NotEmpty val selectedProblemAreaCodes: List<String>? = null,
    @field:[NotEmpty Valid] val answers: List<SelfTestAnswerDto>? = null,
)

data class SelfTestAnswerDto(
    @field:NotEmpty val questionCode: String? = null,
    @Schema(description = "Only for `boolean`")
    val booleanAnswer: Boolean? = null,
    @Schema(description = "Only for `slider`")
    val sliderAnswer: Int? = null,
    @Schema(description = "Only for `single_choice`")
    val singleChoiceAnswerCode: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class SelfTestResultsDto(
    val results: List<SelfTestResultDto>,
    val eligibleForPersonalAssistance: Boolean,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class SelfTestResultDto(
    val problemAreaCode: String,
    val severity: SelfTestSeverity,
    val score: Int,
)

data class SelfTestProblemAreaDto(
    @field:NotEmpty val problemAreaCode: String? = null,
    @field:NotNull val personalAssistance: Boolean? = null,
    @field:NotNull val severity: SelfTestSeverity,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class SelfTestAdviceDto(
    val adviceText: String? = null,
    val chatText: String? = null,
    val mirro: ConfigureSelfTestMirroDto? = null,
)

enum class SelfTestSeverity(val salesforceValue: String) : EnumWithValue {
    BARELY("nauwelijks klachten"),
    LIGHT("lichte klachten"),
    MEDIUM("gemiddelde klachten"),
    SERIOUS("ernstige klachten"),
    ;

    companion object : EnumHelper<SelfTestSeverity>(SelfTestSeverity::class) {
        @JvmStatic
        @JsonCreator
        fun get(value: String) = SelfTestSeverity.fromValue(value)
    }
}
