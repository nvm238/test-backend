package com.innovattic.medicinfo.web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.innovattic.common.error.CommonErrorCodes
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.web.dto.JwtDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

@Component
class AuthenticationService(
    private val dao: UserDao,
    private val clock: Clock,
    @Value("\${jwt.signing_secret}") private val secret: String,
) {
    companion object {
        private const val ISSUER = "innovattic"
        const val USER_ID_CLAIM = "uid"
        const val USERNAME_CLAIM = "una"

        const val SESSION_COOKIE = "medicinfo-session"

        val WEB_VALIDITY: Duration = Duration.ofMinutes(15)
        val APP_VALIDITY: Duration = Duration.ofMinutes(15)
    }

    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private lateinit var jwtAlgorithm: Algorithm
    private lateinit var jwtVerifier: JWTVerifier

    @PostConstruct
    fun init() {
        require(secret.isNotEmpty()) { "jwt.signing_secret not set" }

        jwtAlgorithm = Algorithm.HMAC256(secret)
        val builder = JWT.require(jwtAlgorithm)
                .withIssuer(ISSUER)
                .acceptLeeway(5)
        jwtVerifier = (builder as JWTVerifier.BaseVerification).build(clock)
    }

    fun createJwtDto(user: User): JwtDto {
        return JwtDto(createAccessToken(clock.instant(), user, APP_VALIDITY), "JWT", APP_VALIDITY.seconds)
    }

    fun createCookie(user: User, secure: Boolean = true, crossSite: Boolean = false): ResponseCookie {
        return ResponseCookie.from(SESSION_COOKIE, createAccessToken(clock.instant(), user, WEB_VALIDITY))
            // server-side only, don't allow access in javascript
            .httpOnly(true)
            .path("/")
            .maxAge(WEB_VALIDITY)
            .secure(secure)
            .sameSite(if (crossSite) "none" else "strict")
            .build()
    }

    fun createAccessToken(now: Instant, user: User, validity: Duration): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plus(validity)))
            .withAudience(user.role.value)
            .withClaim(USER_ID_CLAIM, user.publicId.toString())
            .withClaim(USERNAME_CLAIM, user.email)
            .sign(jwtAlgorithm)
    }

    fun verifyJwt(headerValue: String): DecodedJWT {
        try {
            val jwt = jwtVerifier.verify(headerValue)
            log.info("JWT verification successful")
            return jwt
        } catch (ignoreEx: TokenExpiredException) {
            log.info("JWT verification failed: Token expired")
            throw createResponseStatusException(HttpStatus.UNAUTHORIZED, code = CommonErrorCodes.TOKEN_EXPIRED) { "Your token has expired" }
        } catch (e: JWTVerificationException) {
            log.info("JWT verification failed", e)
            throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Your token is not valid" }
        }
    }

    fun verifyApiKey(userId: UUID, apiKey: String) {
        if (dao.apiKeyExists(userId, apiKey)) {
            log.info("API key validation successful for user {}", userId)
        } else {
            log.info("API key validation failed for user {}", userId)
            throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Invalid refresh token" }
        }
    }
}
