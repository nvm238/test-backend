package com.innovattic.medicinfo.logic.dto.salesforce

import com.fasterxml.jackson.annotation.JsonProperty

data class SalesforceGeneralPracticePractitionersDto(
    @JsonProperty("GeneralPractitioners") val list: List<SalesforceGeneralPracticePractitionerDto>
)

data class SalesforceGeneralPracticePractitionerDto(
    @JsonProperty("AGBcode") val code: String,
    @JsonProperty("Name") val name: String
)
