package com.innovattic.medicinfo.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ApiKeyDto(
    @field:NotEmpty val fcmApiKey: String? = null,
)
