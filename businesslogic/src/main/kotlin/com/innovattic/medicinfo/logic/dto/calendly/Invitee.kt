package com.innovattic.medicinfo.logic.dto.calendly

data class Invitee(
    val cancelled: Boolean,
    val charge: String?,
    val email: String,
    val eventEnd: String,
    val eventStart: String,
    val firstName: String?,
    val fullName: String,
    val gcalendarEventUrl: String,
    val icalendarEventUrl: String,
    val id: Int,
    val lastName: String?,
    val locale: String,
    val phoneNumber: String?,
    val timeNotation: String,
    val timezone: String,
    val uri: String,
    val uuid: String
)
