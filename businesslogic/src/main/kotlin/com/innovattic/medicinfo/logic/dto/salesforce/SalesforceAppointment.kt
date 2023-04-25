package com.innovattic.medicinfo.logic.dto.salesforce

data class SalesforceAppointment(
    val appointmentId: String,
    val eventId: String,
    val videoMeetingUrl: String
)
