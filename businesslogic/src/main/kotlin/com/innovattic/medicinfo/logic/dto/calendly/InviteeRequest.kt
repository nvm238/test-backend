package com.innovattic.medicinfo.logic.dto.calendly

data class InviteeRequest(
    val email: String,
    val fullName: String,
    val timeNotation: String,
    val timezone: String
)
