package com.innovattic.medicinfo.logic.dto.calendly

data class Day(
    val date: String,
    val inviteeEvents: List<String>,
    val spots: List<Spot>,
    val status: String
)
