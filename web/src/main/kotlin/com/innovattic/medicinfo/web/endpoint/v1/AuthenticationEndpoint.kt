package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.common.error.failResponseIf
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.web.OpenApiComponent
import com.innovattic.medicinfo.web.dto.JwtDto
import com.innovattic.medicinfo.web.security.AuthenticationService
import com.innovattic.medicinfo.web.security.CookieAuthenticationFilter
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("v1/authentication")
class AuthenticationEndpoint(private val authenticationService: AuthenticationService, private val userDao: UserDao) : BaseEndpoint() {

    companion object {
        const val DIGEST_PREFIX = "Digest "
    }

    @GetMapping("token/{userId}")
    @SecurityRequirement(name = OpenApiComponent.SECURITY_APIKEY)
    @Operation(
        summary = "Get access token",
        description = "Available to any user, to obtain access tokens. " +
            "This app does not use refresh tokens, since their purpose is already covered by API keys." +
            "The Authorization header has the value: apiKey"
    )
    fun getToken(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) authorization: String?,
        @PathVariable("userId") id: UUID,
    ): ResponseEntity<JwtDto> {
        validate(id, authorization)
        return ResponseEntity.ok(authenticationService.createJwtDto(userDao.getByPublicId(id)!!))
    }

    @GetMapping("session/{userId}")
    @SecurityRequirement(name = OpenApiComponent.SECURITY_APIKEY)
    @Operation(summary = "Start session", description = "Available to any user, to start a session (cookie auth)")
    fun getSession(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) authorization: String?,
        @RequestHeader("X-Forwarded-Proto") @Parameter(hidden = true) protocolHeader: String?,
        @RequestHeader(CookieAuthenticationFilter.HEADER_CROSS_SITE_COOKIES) crossSite: String?,
        @PathVariable("userId") id: UUID,
    ): ResponseEntity<Any> {
        validate(id, authorization)
        val cookie = authenticationService.createCookie(userDao.getByPublicId(id)!!, protocolHeader == "https", crossSite != null)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(mapOf("expiresIn" to AuthenticationService.WEB_VALIDITY.seconds))
    }

    @DeleteMapping("session")
    @Operation(summary = "End session", description = "Ends any active session, by deleting the cookie")
    fun deleteSession(): ResponseEntity<Any> {
        val cookie = ResponseCookie.from(AuthenticationService.SESSION_COOKIE, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0) // expire immediately
            .build()
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build()
    }

    private fun validate(userId: UUID, authorization: String?) {
        failResponseIf(authorization.isNullOrBlank(), HttpStatus.UNAUTHORIZED) { "${HttpHeaders.AUTHORIZATION} header is required" }
        var apiToken = authorization
        // We used to have the 'Digest' prefix for the API key value, but that doesn't really make sense, and will
        // also confuse people. Let's accept it for backwards compatibility, but don't require it.
        if (authorization.startsWith(DIGEST_PREFIX)) {
            apiToken = authorization.substring(DIGEST_PREFIX.length)
        }
        authenticationService.verifyApiKey(userId, apiToken)
    }
}
