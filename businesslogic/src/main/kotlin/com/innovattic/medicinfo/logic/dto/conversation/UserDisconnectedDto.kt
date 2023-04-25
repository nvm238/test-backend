package com.innovattic.medicinfo.logic.dto.conversation

import java.util.UUID

data class UserDisconnectedDto(
    override val userId: UUID
) : BaseConversationEvent
