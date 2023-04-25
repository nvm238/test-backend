package com.innovattic.medicinfo.logic.dto

data class PushNotificationDto(
    val title: String? = null,
    val message: String? = null,
    val data: Map<String, Any>? = null,
    val badge: Int? = null,
    val sound: String? = null,
)
