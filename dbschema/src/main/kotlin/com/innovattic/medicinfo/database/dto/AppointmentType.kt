package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class AppointmentType : EnumWithValue {
    INTAKE,
    REGULAR;

    companion object : EnumHelper<AppointmentType>(AppointmentType::class) {
        @JvmStatic
        @JsonCreator
        fun get(value: String) = AppointmentType.fromValue(value)
    }
}
