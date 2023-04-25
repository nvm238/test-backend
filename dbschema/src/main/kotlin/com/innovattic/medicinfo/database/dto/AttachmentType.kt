package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class AttachmentType : EnumWithValue {
    IMAGE,
    ;

    companion object : EnumHelper<AttachmentType>(AttachmentType::class) {
        @JvmStatic
        @JsonCreator
        fun get(value: String) = fromValue(value)
    }
}
