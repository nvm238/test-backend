package com.innovattic.medicinfo.logic.dto.salesforce

import com.fasterxml.jackson.annotation.JsonProperty

data class SalesforceGeneralPracticesDto(
    @JsonProperty("GeneralPractices") val list: List<SalesforceGeneralPracticeDto>
)

data class SalesforceGeneralPracticeDto(
    @JsonProperty("AGBcode") val code: String,
    @JsonProperty("Name") val name: String
)
