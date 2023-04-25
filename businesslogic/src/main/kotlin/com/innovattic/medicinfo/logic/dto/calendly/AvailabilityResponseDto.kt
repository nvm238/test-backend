package com.innovattic.medicinfo.logic.dto.calendly

data class AvailabilityResponseDto(
    val availabilityTimezone: String,
    val days: List<Day>,
    val today: String
)
