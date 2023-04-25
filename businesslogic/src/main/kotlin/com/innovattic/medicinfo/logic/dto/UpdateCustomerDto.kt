package com.innovattic.medicinfo.logic.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.innovattic.medicinfo.database.dto.CustomerOnboardingDetailsDto
import com.innovattic.medicinfo.database.dto.Gender
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UpdateCustomerDto(
    @Schema(description = "Optional - Display name")
    val displayName: String? = null,
    @Schema(description = "Optional - Email address")
    val email: String? = null,
    @Schema(description = "Optional - Gender: MALE, FEMALE, OTHER")
    val gender: Gender? = null,
    @Schema(description = "Optional - Age, between 0 and 130")
    @field:[Min(0) Max(130)] val age: Int? = null,
    @Schema(description = "Optional - Is user insured")
    val isInsured: Boolean? = null,
    @Schema(
        description = """Optional - FCM key for sending push notifications. 
        If deviceToken is present, the label should support Push Notifications"""
    )
    val deviceToken: String? = null,
    @Schema(description = "Optional - New privacy version accepted")
    val privacyVersion: String? = null,
    @Schema(description = "Optional - New privacy version accepted timestamp")
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
)
