package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ShelterLocationDto(
    @Schema(description = "The id of the shelter location")
    @field:NotBlank val id: String,
    @Schema(description = "The name of the shelter location")
    @field:NotBlank val name: String,
)
