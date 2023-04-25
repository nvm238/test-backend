package com.innovattic.medicinfo.logic.dto.conversation

import java.time.ZonedDateTime
import java.util.UUID

data class ReadDto(
    override val userId: UUID,
    val readAt: ZonedDateTime
) : BaseConversationEvent
