package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Null
import javax.validation.constraints.Pattern

interface UserDto {
    val id: UUID?
    val created: ZonedDateTime?
    val displayName: String?
    val role: UserRole
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AdminDto(
    @field:Null override val id: UUID? = null,
    @field:Null override val created: ZonedDateTime? = null,
    @field:NotEmpty override val displayName: String? = null,
    @field:NotEmpty val email: String? = null,
) : UserDto {
    override val role = UserRole.ADMIN
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class EmployeeDto(
    @field:Null override val id: UUID? = null,
    @field:Null override val created: ZonedDateTime? = null,
    @field:NotEmpty override val displayName: String? = null,
) : UserDto {
    override val role = UserRole.EMPLOYEE
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CustomerDto(
    @Schema(description = "Required - Customer UUID")
    override val id: UUID? = null,
    @Schema(description = "Required - Customer created at timestamp")
    override val created: ZonedDateTime? = null,
    @Schema(description = "Required - Display name")
    override val displayName: String? = null,
    @Schema(description = "Required - Label UUID ")
    val labelId: UUID? = null,
    @Schema(description = "Optional - Email address")
    val email: String? = null,
    @Schema(description = "Optional - Gender: MALE, FEMALE, OTHER")
    val gender: Gender? = null,
    @Schema(description = "Optional - Age")
    val age: Int? = null,
    @Schema(description = "Optional - Is the customer insured")
    val isInsured: Boolean? = null,
    @Schema(description = "FCM key for sending push notifications")
    val deviceToken: String? = null,
    @Schema(description = "Optional - Accepted privacy version")
    val privacyVersion: String? = null,
    @Schema(description = "Optional - Accepted privacy timestamp")
    val privacyVersionAcceptedAt: ZonedDateTime? = null,
    @Schema(description = "Optional - Customer entry details")
    val customerOnboardingDetails: CustomerOnboardingDetailsDto? = null,
    @Schema(description = "Optional - Customer date of birth")
    val birthdate: ZonedDateTime? = null,
    @Schema(description = "Optional - Customer phone number")
    val phoneNumber: String? = null,
    @Schema(description = "Optional - Customer postal code")
    val postalCode: String? = null,
    @Schema(description = "Optional - Customer house number")
    val houseNumber: String? = null
) : UserDto {
    override val role = UserRole.CUSTOMER
}

data class CustomerOnboardingDetailsDto(
    val customerEntryType: CustomerEntryType? = null,
    @Schema(description = "General practice center code as received from the /general-practice/center endpoint")
    val generalPracticeCenterAGBcode: String? = null,
    @Schema(description = "General practice center name as received from the /general-practice/center endpoint")
    val generalPracticeCenter: String? = null,
    @Schema(description = "General practice code as received from the /general-practice/practice endpoint")
    val generalPracticeAGBcode: String? = null,
    @Schema(description = "General practice name as received from the /general-practice/practice endpoint")
    val generalPractice: String? = null,
    @Schema(description = "General practice practitioner code as received from the /general-practice/practice/{code}/practitioner endpoint")
    val generalPracticePractitionerAGBcode: String? = null,
    @Schema(description = "General practice practitioner name as received from the /general-practice/practice/{code}/practitioner endpoint")
    val generalPracticePractitioner: String? = null,
    val holidayDestination: String? = null,
    @Schema(description = "The shelter location id in case of ukrainian refugees as received from the /shelter-location endpoint")
    val shelterLocationId: String? = null,
    @Schema(description = "The shelter location name in case of ukrainian refugees as received from the /shelter-location endpoint")
    val shelterLocationName: String? = null,
    @field:Pattern(regexp = "(\\d){9}") val bsn: String? = null,
    val lastName: String? = null
)
