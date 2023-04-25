package com.innovattic.medicinfo.logic.dto.conversation

import java.time.ZonedDateTime
import java.util.UUID

data class ReceivedDto(
    override val userId: UUID,
    val receivedAt: ZonedDateTime
) : BaseConversationEvent
