package com.innovattic.medicinfo.logic.dto.migration

import java.util.UUID

data class MigratedDto(
    val userId: UUID,
    val conversationId: UUID,
    val apiKey: String
)
