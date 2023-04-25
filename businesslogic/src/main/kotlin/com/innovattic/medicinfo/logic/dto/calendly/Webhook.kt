package com.innovattic.medicinfo.logic.dto.calendly

data class Webhook(
    val uri: String,
    val callbackUrl: String,
    val events: List<String>
)
