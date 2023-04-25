package com.innovattic.medicinfo.logic.dto.calendly

data class AppointmentRequestDto(
    val event: EventRequest,
    val eventTypeUuid: String,
    val invitee: InviteeRequest
)
