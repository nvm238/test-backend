package com.innovattic.medicinfo.logic.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UpdateLabelDto(
    @Schema(description = "Optional - Label code")
    val code: String? = null,
    @Schema(description = "Optional - Label display name")
    val name: String? = null,
)
