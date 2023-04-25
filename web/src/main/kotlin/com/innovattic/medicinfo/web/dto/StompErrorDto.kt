package com.innovattic.medicinfo.web.dto

import org.springframework.http.HttpStatus

data class StompErrorDto(
    val status: HttpStatus,
    val errorCode: String?
)
