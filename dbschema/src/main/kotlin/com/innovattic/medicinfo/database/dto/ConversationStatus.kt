package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class ConversationStatus : EnumWithValue {
    OPEN,
    ARCHIVED,
    ;

    companion object : EnumHelper<ConversationStatus>(ConversationStatus::class) {
        @JvmStatic @JsonCreator fun get(value: String) = fromValue(value)
    }
}
