package com.innovattic.medicinfo.web.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class MirroUrlDto(
    /**
     * Mirro SSO URL with access token appended
     */
    val url: String,
    /**
     * The amount of seconds from now, in which this token expires.
     */
    val expiresIn: Long
)
