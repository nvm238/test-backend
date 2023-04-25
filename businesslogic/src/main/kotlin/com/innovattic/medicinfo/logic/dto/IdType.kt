package com.innovattic.medicinfo.logic.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class IdType(val salesforceValue: String) : EnumWithValue {
    PASSPORT("Paspoort"),
    DRIVERS_LICENSE("Rijbewijs"),
    ID("Identiteitskaart"),
    ALIEN_ID("Vreemdelingendocument"),
    ;

    companion object : EnumHelper<IdType>(IdType::class) {
        @JvmStatic @JsonCreator
        fun get(value: String) = IdType.fromValue(value)
    }
}
