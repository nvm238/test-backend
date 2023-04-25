package com.innovattic.medicinfo.web.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotNull

data class EmailDto(
    @field: Email
    @field: NotNull
    val email: String
)
