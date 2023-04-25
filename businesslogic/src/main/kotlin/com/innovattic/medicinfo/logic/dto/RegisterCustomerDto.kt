package com.innovattic.medicinfo.logic.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.CustomerOnboardingDetailsDto
import com.innovattic.medicinfo.database.dto.Gender
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class RegisterCustomerDto(
    @Schema(description = "Required - Customer display name")
    @field:NotEmpty val displayName: String? = null,
    @Schema(description = "Required - Label id to register customer")
    @field:NotNull val labelId: UUID? = null,
    @Schema(description = "Optional - Customer email")
    val email: String? = null,
    @Schema(description = "Optional - Gender: FEMALE, MALE, OTHER")
    val gender: Gender? = null,
    @Schema(description = "Optional - User age")
    @field:[Min(0) Max(130)] val age: Int? = null,
    @Schema(description = "Optional - Is the user insured")
    val isInsured: Boolean? = null,
    @Schema(
        description = """Optional - FCM key for sending push notifications. 
        If deviceToken is present, the label should support Push Notifications"""
    )
    val deviceToken: String? = null,
    @Schema(description = "Optional - Accepted privacy version")
    val privacyVersion: String? = null,
    @Schema(description = "Deprecated field - it has no effect and will be removed in the future")
    val privacyVersionAcceptedAt: ZonedDateTime? = null,
    @Schema(description = "Optional - Customer entry details")
    @field:Valid val customerOnboardingDetails: CustomerOnboardingDetailsDto? = null,
    @Schema(description = "Optional - Customer date of birth")
    val birthdate: ZonedDateTime? = null,
    @Schema(description = "Optional - Customer phone number")
    val phoneNumber: String? = null,
    @Schema(description = "Optional - Customer postal code")
    val postalCode: String? = null,
    @Schema(description = "Optional - Customer house number")
    val houseNumber: String? = null
) {
    fun toCustomerDto(): CustomerDto = CustomerDto(
        null,
        null,
        displayName,
        labelId,
        email,
        gender,
        age,
        isInsured,
        deviceToken,
        privacyVersion,
        null,
        customerOnboardingDetails,
        birthdate,
        phoneNumber,
        postalCode,
        houseNumber
    )
}
