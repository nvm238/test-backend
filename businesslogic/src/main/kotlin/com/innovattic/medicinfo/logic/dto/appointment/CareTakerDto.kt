package com.innovattic.medicinfo.logic.dto.appointment

data class CareTakerDto(
    val fullName: String,
    val description: String?,
    val avatarUrl: String?,
    val eventTypeId: String,
    val durationInMinutes: Int,
    val availabilities: List<AvailabilityDto>
)
