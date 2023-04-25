package com.innovattic.medicinfo.logic.dto

import com.innovattic.medicinfo.database.dto.UserDto

data class NewUserDto(val apiKey: String, val user: UserDto)
