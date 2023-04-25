package com.innovattic.medicinfo.web.security

import com.auth0.jwt.interfaces.DecodedJWT
import com.innovattic.common.auth.BaseTokenAuthFilter
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.web.AppExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.http.HttpServletRequest

@Component
class JwtAuthenticationFilter(
    private val authenticationService: AuthenticationService,
    provider: AppAuthenticationProvider,
    exceptionHandler: AppExceptionHandler,
) : BaseTokenAuthFilter(provider, exceptionHandler) {
    override fun createToken(authenticationToken: String, request: HttpServletRequest): Authentication {
        return JwtAuthentication(authenticationService.verifyJwt(authenticationToken))
    }
}

class JwtAuthentication(val jwt: DecodedJWT) : AbstractAuthenticationToken(null) {
    lateinit var realUser: User

    override fun getCredentials(): DecodedJWT {
        return jwt
    }

    override fun getPrincipal(): String? {
        return jwt.getClaim(AuthenticationService.USER_ID_CLAIM).asString()
    }

    fun resolveUser(dao: UserDao): User {
        if (!::realUser.isInitialized) {
            realUser = dao.getByPublicId(getUserId()) ?: throw createResponseStatusException(HttpStatus.UNAUTHORIZED) {
                "Access token was valid, but user does not exist"
            }
            details = realUser // not actually used but maybe Spring appreciates it?
        }
        return realUser
    }

    fun getUserId(): UUID {
        return UUID.fromString(principal)
    }

    fun getRole(): UserRole {
        return UserRole.get(jwt.audience.single())!!
    }
}
