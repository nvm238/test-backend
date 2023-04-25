package com.innovattic.medicinfo.logic.dto.salesforce

import com.fasterxml.jackson.annotation.JsonProperty

data class SalesforceGeneralPracticeCentersDto(
    @JsonProperty("GeneralPracticeCenters") val list: List<SalesforceGeneralPracticeCenterDto>
)

data class SalesforceGeneralPracticeCenterDto(
    @JsonProperty("AGBcode") val code: String,
    @JsonProperty("Name") val name: String
)
