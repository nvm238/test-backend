package com.innovattic.medicinfo.web.security

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.UserDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*

@Component
class AppAuthenticationProvider(private val userDao: UserDao) : AuthenticationProvider {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun authenticate(authentication: Authentication?): Authentication? {
        if (authentication !is JwtAuthentication) return authentication

        val jwt = authentication.credentials
        val userIdClaim = jwt.claims[AuthenticationService.USER_ID_CLAIM]
                ?: error("JWT without ${AuthenticationService.USER_ID_CLAIM} claim")

        val user = userDao.getByPublicId(UUID.fromString(userIdClaim.asString()))
            ?: throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Token was valid, but user does not exist" }
        authentication.details = user

        log.info("JWT authentication completed for {} user {}", user.role, user.publicId)
        authentication.isAuthenticated = true
        return authentication
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication.isAssignableFrom(JwtAuthentication::class.java)
    }
}
