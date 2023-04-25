package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class Gender : EnumWithValue {
    FEMALE,
    MALE,
    OTHER,
    ;

    companion object : EnumHelper<Gender>(Gender::class) {
        @JvmStatic @JsonCreator fun get(value: String) = fromValue(value)
    }
}
