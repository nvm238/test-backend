package com.innovattic.medicinfo.dto

import com.innovattic.medicinfo.database.dto.AdminDto
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.EmployeeDto
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.database.dto.UserRole
import java.time.ZonedDateTime
import java.util.*

data class NewAdminDto(val apiKey: String, val user: AdminDto)
data class NewEmployeeDto(val apiKey: String, val user: EmployeeDto)
data class NewCustomerDto(val apiKey: String, val user: CustomerDto)
data class AnyUserDto(
    val id: UUID? = null,
    val created: ZonedDateTime? = null,
    val displayName: String? = null,
    val role: UserRole? = null,
    val labelId: UUID? = null,
    val email: String? = null,
    val gender: Gender? = null,
    val age: Int? = null,
    val isInsured: Boolean? = null,
    val deviceToken: String? = null,
)
