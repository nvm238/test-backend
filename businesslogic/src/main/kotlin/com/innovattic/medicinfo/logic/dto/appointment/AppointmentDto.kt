package com.innovattic.medicinfo.logic.dto.appointment

import com.innovattic.medicinfo.database.dto.AppointmentType
import java.time.ZonedDateTime
import java.util.UUID

data class AppointmentDto(
    val id: UUID,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val canceledAt: ZonedDateTime?,
    val cancelReason: String?,
    val videoMeetingUrl: String?,
    val appointmentType: AppointmentType?,
    val creationFailed: Boolean
)
