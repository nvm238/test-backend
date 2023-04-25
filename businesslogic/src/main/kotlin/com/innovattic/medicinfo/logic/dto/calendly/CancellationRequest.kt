package com.innovattic.medicinfo.logic.dto.calendly

data class CancellationRequest(
    val cancelReason: String,
    val canceledBy: String
)
