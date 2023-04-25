package com.innovattic.medicinfo.logic.dto.calendly

import java.time.ZonedDateTime

data class CalendlyUserDto(
    val avatarUrl: String?,
    val createdAt: ZonedDateTime,
    val currentOrganization: String,
    val email: String,
    val name: String,
    val schedulingUrl: String,
    val slug: String,
    val timezone: String,
    val updatedAt: ZonedDateTime,
    val uri: String
)
