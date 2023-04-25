package com.innovattic.medicinfo.logic.dto

import java.time.ZonedDateTime

data class ServiceAvailableDto(
    val serviceAvailable: Boolean,
    val nextOpeningTime: ZonedDateTime? = null
)
