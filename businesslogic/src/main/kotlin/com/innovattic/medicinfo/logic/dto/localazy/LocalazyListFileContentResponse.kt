package com.innovattic.medicinfo.logic.dto.localazy

import com.fasterxml.jackson.annotation.JsonProperty

data class LocalazyListFileContentResponse(
    @JsonProperty("keys") val keys: List<LocalazyTranslationKey>,
    @JsonProperty("next") val next: String?,
)

data class LocalazyTranslationKey(
    @JsonProperty("id") val id: String,
    @JsonProperty("key") val key: List<String>,
    @JsonProperty("value") val value: String,
    @JsonProperty("vid") val vid: String,

)
