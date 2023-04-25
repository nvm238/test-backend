package com.innovattic.medicinfo.logic.dto.calendly

data class CustomQuestion(
    val enabled: Boolean,
    val includeOther: Boolean,
    val name: String,
    val position: Int,
    val required: Boolean,
    val type: String
)
