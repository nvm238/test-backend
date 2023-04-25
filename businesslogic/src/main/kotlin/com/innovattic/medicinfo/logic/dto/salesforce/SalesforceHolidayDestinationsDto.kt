package com.innovattic.medicinfo.logic.dto.salesforce

import com.fasterxml.jackson.annotation.JsonProperty

data class SalesforceHolidayDestinationsDto(
    @JsonProperty("holidayDestinations") val list: List<SalesforceHolidayDestinationDto>
)

data class SalesforceHolidayDestinationDto(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String
)
