package com.innovattic.medicinfo.web.integration.mirro

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.innovattic.common.error.CommonErrorCodes
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.web.dto.MirroUrlDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.convert.DurationUnit
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.annotation.PostConstruct

@Component
class MirroTokenService(
    private val clock: Clock,
    private val mirroKeyProvider: MirroKeyProvider,
    @Value("\${mirro.jwt.expiration.seconds}") @DurationUnit(ChronoUnit.SECONDS) private val tokenExpiration: Duration,
    @Value("\${mirro.organisationId}") private val organisationId: Int,
    @Value("\${mirro.loginUrl}") private val mirroLoginUrl: String,
) {
    companion object {
        private const val ISSUER = "medicinfo"
        const val USER_CLAIM_OUTER = "user"
        const val USER_CLAIM_INNER = "guid"
        const val ORGANIZATION_ID = "organisationId"
        const val BLOG_ID = "blogId"
    }

    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private lateinit var jwtAlgorithm: Algorithm
    private lateinit var jwtVerifier: JWTVerifier

    @PostConstruct
    fun init() {
        jwtAlgorithm = Algorithm.RSA256(mirroKeyProvider)
        val builder = JWT.require(jwtAlgorithm)
            .withIssuer(ISSUER)
            .acceptLeeway(5)
        jwtVerifier = (builder as JWTVerifier.BaseVerification).build(clock)
    }

    fun createMirroSSOUrl(user: User, blogId: Int): MirroUrlDto {
        val token = createAccessToken(clock.instant(), user, blogId, tokenExpiration)
        return MirroUrlDto(combineUrlWithToken(token), tokenExpiration.toSeconds())
    }

    private fun combineUrlWithToken(token: String): String {
        return if (mirroLoginUrl.endsWith("/")) "$mirroLoginUrl$token" else "$mirroLoginUrl/$token"
    }

    fun createAccessToken(now: Instant, user: User, blogId: Int, validity: Duration): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plus(validity)))
            .withClaim(USER_CLAIM_OUTER, mapOf(USER_CLAIM_INNER to user.publicId.toString()))
            .withClaim(ORGANIZATION_ID, organisationId)
            .withClaim(BLOG_ID, blogId)
            .sign(jwtAlgorithm)
    }

    fun verifyJwt(token: String): DecodedJWT {
        try {
            val jwt = jwtVerifier.verify(token)
            log.info("JWT verification successful")
            return jwt
        } catch (ignoreEx: TokenExpiredException) {
            log.info("Mirro JWT verification failed: Token expired")
            throw createResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                code = CommonErrorCodes.TOKEN_EXPIRED
            ) { "Your token has expired" }
        } catch (e: JWTVerificationException) {
            log.info("Mirro JWT verification failed", e)
            throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Your token is not valid" }
        }
    }
}
