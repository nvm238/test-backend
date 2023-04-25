package com.innovattic.medicinfo.web.security

import com.innovattic.common.web.configureSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.transaction.PlatformTransactionManager

@Configuration
open class SecurityConfig(
    private val transactionMgr: PlatformTransactionManager,
    private val authFilter: JwtAuthenticationFilter,
    private val cookieAuthFilter: CookieAuthenticationFilter,
) {

    @Bean
    open fun configureSecurity(http: HttpSecurity): SecurityFilterChain {
        configureSecurity(
            http, authFilter, transactionMgr,
            "/info/**", // Unauthenticated endpoints for general server info

            // swagger
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",

            "/v1/appointment/callback", // custom callback signature checked in service
            "/v1/chat/**", // custom auth for websockets
            "/v1/label/code/*", // get label by code for apps
            "/v1/user/customer/register", // custom auth checked in service
            "/v1/migrate", // custom auth checked in service
            "/v1/authentication/**", // login endpoints
            "/v1/general-practice/**", // general practice endpoints
            "/v1/holiday-destination/**", // holiday destination endpoints
            "/v1/shelter-location/**", // Shelter location endpoints
        )

        http.addFilterBefore(cookieAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
