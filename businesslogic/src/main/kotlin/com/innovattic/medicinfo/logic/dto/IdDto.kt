package com.innovattic.medicinfo.logic.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class IdDto(
    @field:NotNull val idType: IdType? = null,
    @field:NotEmpty val idNumber: String? = null,
)
