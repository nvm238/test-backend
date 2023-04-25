package com.innovattic.medicinfo.web.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class JwtDto(
    /**
     * Access token
     */
    val accessToken: String,
    /**
     * Token type. Is always 'JWT' currently.
     */
    val tokenType: String,
    /**
     * The amount of seconds from now, in which this token expires.
     */
    val expiresIn: Long
)
