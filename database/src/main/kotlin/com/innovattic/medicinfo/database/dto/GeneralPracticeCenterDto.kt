package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class GeneralPracticeCenterDto(
    @Schema(description = "The AGB Code of the general practice center")
    @field:NotEmpty val code: String,
    @Schema(description = "The name of the general practice center")
    @field:NotEmpty val name: String,
)
