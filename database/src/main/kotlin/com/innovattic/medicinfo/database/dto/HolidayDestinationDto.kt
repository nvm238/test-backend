package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class HolidayDestinationDto(
    @Schema(description = "The id of the holiday destination")
    @field:NotEmpty val id: String,
    @Schema(description = "The name of the holiday destination")
    @field:NotEmpty val name: String,
)
