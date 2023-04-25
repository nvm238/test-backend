package com.innovattic.medicinfo.database.dto

import java.time.ZonedDateTime
import java.util.UUID

data class ConversationDto(
    val id: UUID,
    val created: ZonedDateTime,
    val customer: CustomerInfoDto,
    val deliveredToCustomer: ZonedDateTime?,
    val deliveredToEmployee: ZonedDateTime?,
    val readByCustomer: ZonedDateTime?,
    val readByEmployee: ZonedDateTime?,
    val status: ConversationStatus,
    val messages: List<MessageDto>
)

data class CustomerInfoDto(
    val id: UUID,
    val name: String,
    val gender: Gender?,
    val age: Int?
)
