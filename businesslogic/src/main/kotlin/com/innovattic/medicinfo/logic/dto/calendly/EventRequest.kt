package com.innovattic.medicinfo.logic.dto.calendly

data class EventRequest(
    val locationConfiguration: LocationConfiguration,
    val startTime: String
)
