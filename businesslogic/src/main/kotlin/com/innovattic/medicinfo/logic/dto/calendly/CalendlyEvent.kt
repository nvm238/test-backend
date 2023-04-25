package com.innovattic.medicinfo.logic.dto.calendly

import java.time.ZonedDateTime

data class CalendlyEvent(
    val active: Boolean,
    val color: String,
    val createdAt: ZonedDateTime,
    val customQuestions: List<CustomQuestion>,
    val descriptionHtml: String?,
    val descriptionPlain: String?,
    val duration: Int,
    val internalNote: String?,
    val kind: String,
    val name: String,
    val poolingType: String?,
    val profile: Profile,
    val schedulingUrl: String,
    val secret: Boolean,
    val slug: String,
    val type: String,
    val updatedAt: ZonedDateTime,
    val uri: String
)
