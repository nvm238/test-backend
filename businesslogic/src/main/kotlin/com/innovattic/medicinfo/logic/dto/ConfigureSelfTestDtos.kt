package com.innovattic.medicinfo.logic.dto

import com.innovattic.medicinfo.database.dto.Swagger
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ConfigureSelfTestDto(
    @field:[NotNull Valid] val introduction: ConfigureSelfTestIntroductionDto? = null,
    @field:[NotNull Valid] val problemAreaQuestion: ConfigureSelfTestProblemAreasDto? = null,
    @field:[NotEmpty Valid] val generalQuestions: List<ConfigureSelfTestQuestionDto>? = null,
    @Schema(description = "The type of this question must be `boolean`")
    @field:[NotNull Valid] val personalAssistance: ConfigureSelfTestQuestionDto? = null,
    @field:[NotEmpty] val generalAdviceForBarely: String,
)

data class ConfigureSelfTestIntroductionDto(
    @field:NotEmpty val text: String? = null,
)

data class ConfigureSelfTestProblemAreasDto(
    @field:NotEmpty val title: String? = null,
    @field:NotEmpty val description: String? = null,
    @field:[NotEmpty Valid] val problemAreas: List<ConfigureSelfTestProblemAreaDto>? = null,
    @field:NotNull val maximumAreas: Int? = null,
)

data class ConfigureSelfTestProblemAreaDto(
    @field:NotEmpty val code: String? = null,
    @field:NotEmpty val name: String? = null,
    @field:[NotEmpty Valid] val questions: List<ConfigureSelfTestQuestionDto>? = null,
    @field:[NotEmpty Valid] val outcomes: List<ConfigureSelfTestOutcomeDto>? = null,
    @field:[NotNull Valid] val mirro: ConfigureMirroDto? = null,
    val title: String? = null,
)

data class ConfigureMirroDto(
    @field:NotEmpty val title: String? = null,
    @field:NotEmpty val shortDescription: String? = null,
    @field:NotEmpty val longDescription: String? = null,
    @field:NotEmpty val link: String? = null,
)

data class ConfigureSelfTestOutcomeDto(
    @field:NotNull val severity: SelfTestSeverity? = null,
    @field:NotEmpty val adviceText: String? = null,
    val chatText: String? = null,
    @field:NotNull val mirro: ConfigureSelfTestMirroDto? = null,
    val personalAssistance: Boolean? = null
)

data class ConfigureSelfTestMirroDto(
    @field:NotNull val use: Boolean? = null,
    val description: String? = null,
)

data class ConfigureSelfTestQuestionDto(
    @field:NotEmpty val code: String? = null,
    @field:NotNull val type: SelfTestQuestionType? = null,
    @field:NotEmpty val title: String? = null,
    val description: String? = null,
    @field:[NotEmpty Size(min = 2) Valid] val answers: List<ConfigureSelfTestValueDto>? = null,
)

data class ConfigureSelfTestValueDto(
    @Schema(description = "Only for `single_choice`. Codes must be unique within the question (not globally)")
    val code: String? = null,
    @Schema(description = Swagger.SELF_TEST_VALUE)
    @field:NotNull val value: Any? = null,
    @field:[NotNull Min(0) Max(10)] val score: Int? = null,
)
