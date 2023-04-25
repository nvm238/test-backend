package com.innovattic.medicinfo.logic.dto.calendly

import java.time.ZonedDateTime

data class Event(
    val activeNotificationType: String,
    val assignedTo: List<String>,
    val color: String,
    val endTime: ZonedDateTime,
    val eventTypeKind: String,
    val guests: List<String>,
    val id: Int,
    val location: String?,
    val locationAdditionalInfo: String?,
    val locationType: String,
    val name: String,
    val publishersList: String,
    val scheduledProfile: ScheduledProfile,
    val startTime: ZonedDateTime,
    val uri: String
)
