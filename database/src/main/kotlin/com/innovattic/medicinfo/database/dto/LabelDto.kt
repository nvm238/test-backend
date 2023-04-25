package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Null

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class LabelDto(
    @Schema(description = "Required - Id of the label")
    @field:Null val id: UUID? = null,
    @Schema(description = "Required - Created timestamp of label")
    @field:Null val created: ZonedDateTime? = null,
    @Schema(description = "Required - Label code")
    @field:NotEmpty val code: String? = null,
    @Schema(description = "Required - Label display name")
    @field:NotEmpty val name: String? = null,
    @Schema(description = "Required - Push notifications enabled")
    @field:Null val hasPushNotifications: Boolean? = null,
    val fcmApiKey: String? = null,
)
