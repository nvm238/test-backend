package com.innovattic.medicinfo.logic.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class SelfTestQuestionType : EnumWithValue {
    BOOLEAN,
    SLIDER,
    SINGLE_CHOICE,
    ;

    companion object : EnumHelper<SelfTestQuestionType>(SelfTestQuestionType::class) {
        @JvmStatic @JsonCreator fun get(value: String) = SelfTestQuestionType.fromValue(value)
    }
}
