package com.innovattic.medicinfo.logic.dto.conversation

import java.util.UUID

data class StartTypingDto(
    override val userId: UUID
) : BaseConversationEvent
