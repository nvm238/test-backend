package com.innovattic.medicinfo.logic.dto.calendly

import java.time.ZonedDateTime

data class EventResponseDto(
    val createdAt: ZonedDateTime,
    val endTime: ZonedDateTime,
    val eventMemberships: List<EventMembership>,
    val eventType: String,
    val inviteesCounter: InviteesCounter,
    val name: String,
    val startTime: ZonedDateTime,
    val status: String,
    val updatedAt: ZonedDateTime,
    val uri: String
)
