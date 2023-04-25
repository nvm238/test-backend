package com.innovattic.medicinfo.logic.dto

import java.time.ZonedDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class IdDataDto(
    @field:NotNull val idType: IdType? = null,
    @field:NotEmpty val idNumber: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    @field:Pattern(regexp = "(\\d){9}") val bsn: String? = null,
    val birthDate: ZonedDateTime? = null,
)
