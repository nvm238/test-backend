package com.innovattic.medicinfo.logic.dto.calendly

import java.time.ZonedDateTime

data class InviteeResponse(
    val cancelUrl: String,
    val createdAt: ZonedDateTime,
    val email: String,
    val event: String,
    val firstName: String?,
    val lastName: String?,
    val name: String,
    val newInvitee: String?,
    val oldInvitee: String?,
    val payment: String?,
    val rescheduleUrl: String,
    val rescheduled: Boolean,
    val status: String,
    val timezone: String,
    val updatedAt: ZonedDateTime,
    val uri: String
)
