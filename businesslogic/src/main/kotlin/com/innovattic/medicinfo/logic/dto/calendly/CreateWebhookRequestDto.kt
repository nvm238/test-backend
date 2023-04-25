package com.innovattic.medicinfo.logic.dto.calendly

data class CreateWebhookRequestDto(
    val url: String,
    val events: List<String>,
    val organization: String,
    val scope: String,
    val signingKey: String
)
