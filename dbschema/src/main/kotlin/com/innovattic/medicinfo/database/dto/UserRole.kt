package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class UserRole : EnumWithValue {
    ADMIN,
    EMPLOYEE,
    CUSTOMER,
    ;

    companion object : EnumHelper<UserRole>(UserRole::class) {
        @JvmStatic @JsonCreator fun get(value: String) = fromValue(value)
    }
}
