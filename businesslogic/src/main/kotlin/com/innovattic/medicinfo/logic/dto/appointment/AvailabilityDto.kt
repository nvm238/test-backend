package com.innovattic.medicinfo.logic.dto.appointment

data class AvailabilityDto(
    val day: String,
    val slots: List<SlotDto>
)
