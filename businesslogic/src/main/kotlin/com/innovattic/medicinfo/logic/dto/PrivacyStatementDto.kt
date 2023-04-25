package com.innovattic.medicinfo.logic.dto

import java.time.LocalDateTime

data class PrivacyStatementDto(
    val version: String?,
    val acceptedAt: LocalDateTime?,
)
