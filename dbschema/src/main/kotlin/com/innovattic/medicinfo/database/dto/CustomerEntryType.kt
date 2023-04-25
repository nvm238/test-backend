package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class CustomerEntryType(val salesforceTranslation: String) : EnumWithValue {
    OTHER("anders"),
    GENERAL_PRACTICE_CENTER("Huisartsenpost"),
    GENERAL_PRACTICE("Huisartsenpraktijk"),
    HOLIDAY_TOURIST("Vakantie – NL toerist"),
    HOLIDAY_FOREIGN_TOURIST("Vakantie – buitenlandse toerist"),
    UKRAINIAN_REFUGEE("Oekraïne")
    ;

    companion object : EnumHelper<CustomerEntryType>(CustomerEntryType::class) {
        @JvmStatic @JsonCreator fun get(value: String) = fromValue(value)
    }
}
