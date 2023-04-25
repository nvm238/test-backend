package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class GeneralPracticePractitionerDto(
    @Schema(description = "The AGB Code of the general practice practitioner")
    @field:NotEmpty val code: String,
    @Schema(description = "The name of the general practice practitioner")
    @field:NotEmpty val name: String,
)
