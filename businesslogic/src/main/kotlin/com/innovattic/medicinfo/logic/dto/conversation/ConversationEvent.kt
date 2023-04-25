package com.innovattic.medicinfo.logic.dto.conversation

import com.innovattic.medicinfo.database.dto.MessageDto

data class ConversationEvent(
    val read: ReadDto? = null,
    val received: ReceivedDto? = null,
    val message: MessageDto? = null,
    val startTyping: StartTypingDto? = null,
    val stopTyping: StopTypingDto? = null,
    val userConnected: UserConnectedDto? = null,
    val userDisconnected: UserDisconnectedDto? = null
)
