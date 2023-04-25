package com.innovattic.medicinfo.logic.dto.calendly

import java.time.ZonedDateTime

data class Spot(
    val inviteesRemaining: Int,
    val startTime: ZonedDateTime,
    val status: String
)
