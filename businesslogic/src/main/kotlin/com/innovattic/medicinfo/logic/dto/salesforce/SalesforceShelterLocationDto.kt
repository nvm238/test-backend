package com.innovattic.medicinfo.logic.dto.salesforce

import com.fasterxml.jackson.annotation.JsonProperty

data class SalesforceShelterLocationListDto(
    @JsonProperty("shelterLocations") val list: List<SalesforceShelterLocationDto>
)

data class SalesforceShelterLocationDto(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String
)
