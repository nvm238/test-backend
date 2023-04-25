package com.innovattic.medicinfo.web.security

import com.innovattic.common.error.failResponseIf
import com.innovattic.medicinfo.database.dto.UserRole
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import java.security.Principal

fun verifyRole(vararg roles: UserRole): JwtAuthentication {
    val authentication = verifyAuthentication()
    failResponseIf(authentication.getRole() !in roles, HttpStatus.FORBIDDEN) {
        "Need to be user of type ${roles.joinToString("/") { it.value }}"
    }
    return authentication
}

fun verifyAuthentication(): JwtAuthentication {
    val authentication = SecurityContextHolder.getContext().authentication
    require(authentication?.isAuthenticated == true)
    require(authentication is JwtAuthentication)
    return authentication
}

fun verifyAuthentication(authentication: Principal): JwtAuthentication {
    require(authentication is JwtAuthentication)
    require(authentication.isAuthenticated)
    return authentication
}
