package com.innovattic.medicinfo.logic.dto.conversation

import java.util.UUID

data class UserConnectedDto(
    override val userId: UUID
) : BaseConversationEvent
