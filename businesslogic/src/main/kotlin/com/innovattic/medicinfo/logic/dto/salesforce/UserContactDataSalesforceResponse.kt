package com.innovattic.medicinfo.logic.dto.salesforce

import java.time.LocalDate
import java.util.*

data class UserContactDataSalesforceResponse(
    val birthdate: LocalDate?,
    val inactive: Boolean,
    val customerId: UUID,
    val proposition: String? = null,
    val generalPractice: String? = null,
    val generalPracticeAGBcode: String? = null,
    val generalPracticeCenter: String? = null,
    val generalPracticeCenterAGBcode: String? = null,
    val holidayDestination: String? = null,
    val shelterLocationId: String? = null,
    val shelterLocationName: String? = null,
)
