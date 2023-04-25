package com.innovattic.medicinfo.logic.dto.calendly

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CalendlyCallbackDto(
    val createdAt: ZonedDateTime,
    val event: String,
    val payload: CalendlyPayload
)

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CalendlyPayload(
    val cancelUrl: String,
    val createdAt: ZonedDateTime,
    val email: String,
    val event: String,
    val name: String,
    val status: String,
    val newInvitee: String?,
    val rescheduled: Boolean,
    val cancellation: Cancellation,
    val uri: String
)

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Cancellation(
    val canceledBy: String?,
    val reason: String?
)
