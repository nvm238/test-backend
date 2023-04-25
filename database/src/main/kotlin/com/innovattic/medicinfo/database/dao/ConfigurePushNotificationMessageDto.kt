package com.innovattic.medicinfo.database.dao

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ConfigurePushNotificationMessageDto(
    @field:NotEmpty val text: String? = null,
)
