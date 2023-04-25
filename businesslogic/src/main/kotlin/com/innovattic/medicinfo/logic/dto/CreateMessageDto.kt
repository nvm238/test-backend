package com.innovattic.medicinfo.logic.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.innovattic.medicinfo.database.dto.ActionDto
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CreateMessageDto(
    @Schema(description = "Message text to send. Emoticons are supported.")
    val message: String? = null,
    @Schema(
        description = """Optional translation of the message, in case the original message is not in the language of 
the recipient. Only used internally - don't set this field from the apps."""
    )
    val translatedMessage: String? = null,
    @Schema(description = "Message action, only allowed for Employees/Admins, not Customers.")
    val action: ActionDto? = null,
)
