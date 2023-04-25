package com.innovattic.medicinfo.logic.dto.calendly

data class EventTypesResponseDto(
    val collection: List<CalendlyEvent>,
    val pagination: Pagination
)
