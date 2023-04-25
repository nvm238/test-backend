package com.innovattic.medicinfo.web.security

import com.innovattic.medicinfo.web.AppExceptionHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.server.ResponseStatusException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class CookieAuthenticationFilter(
    private val authenticationService: AuthenticationService,
    private val provider: AppAuthenticationProvider,
    private val exceptionHandler: AppExceptionHandler,
) : OncePerRequestFilter() {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // If the Authorization header is set, skip the cookie - it'll be confusing to have two sources of auth
        // If the X-IgnoreCookie header is set, the client specifically wants us to ignore it - for example,
        // when it's trying to get a fresh cookie.
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null || request.getHeader(HEADER_IGNORE_COOKIE) != null) {
            log.debug("Ignoring cookie in this request")
            filterChain.doFilter(request, response)
            return
        }

        val sessionCookie = request.cookies?.firstOrNull { it.name == AuthenticationService.SESSION_COOKIE }
        sessionCookie?.let {
            try {
                val accessToken = sessionCookie.value
                val decodedJwt = authenticationService.verifyJwt(accessToken)
                val token = JwtAuthentication(decodedJwt)
                log.info("Cookie authentication for user ${token.principal} successful")
                SecurityContextHolder.getContext().authentication = provider.authenticate(token)
            } catch (e: ResponseStatusException) {
                log.info("Cookie auth failed; sending back access-denied")
                // It's important to raise the exception (and respond with 401),
                // as this will let the client know it's cookie is no longer valid and needs to re-login.
                exceptionHandler.handleFromFilter(request, response, e)
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        const val HEADER_IGNORE_COOKIE = "X-IgnoreCookie"
        const val HEADER_CROSS_SITE_COOKIES = "X-CrossSiteCookies"
    }
}
