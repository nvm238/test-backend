package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertEmptyResponse
import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.web.BaseEndpointTest
import com.innovattic.medicinfo.web.security.AuthenticationService
import com.innovattic.medicinfo.web.security.CookieAuthenticationFilter
import io.restassured.matcher.RestAssuredMatchers
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.util.*

class AuthenticationEndpointTest : BaseEndpointTest() {
    @Test
    fun getToken_works_forAdmin() {
        Given {
            header(HttpHeaders.AUTHORIZATION, "Digest ${apiKey(userService.adminUser)}")
        } When {
            get("v1/authentication/token/${userService.adminUser.publicId}")
        } Then {
            assertObjectResponse()
            body("accessToken", notNullValue())
            body("refreshToken", nullValue())
            body("expiresIn", equalTo(AuthenticationService.APP_VALIDITY.seconds.toInt()))
        }
    }

    @Test
    fun getToken_works_forEmployee() {
        val user = createEmployee("e1")
        Given {
            header(HttpHeaders.AUTHORIZATION, "Digest ${apiKey(user)}")
        } When {
            get("v1/authentication/token/${user.publicId}")
        } Then {
            assertObjectResponse()
            body("accessToken", notNullValue())
            body("refreshToken", nullValue())
            body("expiresIn", equalTo(AuthenticationService.APP_VALIDITY.seconds.toInt()))
        }
    }

    @Test
    fun getToken_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        Given {
            header(HttpHeaders.AUTHORIZATION, "Digest ${apiKey(user)}")
        } When {
            get("v1/authentication/token/${user.publicId}")
        } Then {
            assertObjectResponse()
            body("accessToken", notNullValue())
            body("refreshToken", nullValue())
            body("expiresIn", equalTo(AuthenticationService.APP_VALIDITY.seconds.toInt()))
        }
    }

    @Test
    fun getToken_fails_forInvalidApiKey() {
        Given {
            header(HttpHeaders.AUTHORIZATION, "Digest ${UUID.randomUUID()}")
        } When {
            get("v1/authentication/token/${userService.adminUser.publicId}")
        } Then {
            assertErrorResponse(null, HttpStatus.UNAUTHORIZED)
        }
    }

    @Test
    fun getSession_works() {
        val user = createCustomer(createLabel(), "c2")
        Given {
            header(HttpHeaders.AUTHORIZATION, "Digest ${apiKey(user)}")
        } When {
            get("v1/authentication/session/${user.publicId}")
        } Then {
            assertObjectResponse()
            body("expiresIn", equalTo(AuthenticationService.APP_VALIDITY.seconds.toInt()))
            cookie(AuthenticationService.SESSION_COOKIE, RestAssuredMatchers.detailedCookie()
                .httpOnly(true)
                .path("/")
                .maxAge(AuthenticationService.WEB_VALIDITY.seconds)
                .secured(false) // tests don't run on https
                .sameSite("strict"))
        }
    }

    @Test
    fun getSession_works_forCrossSite() {
        val user = createCustomer(createLabel(), "c3")
        Given {
            header(HttpHeaders.AUTHORIZATION, "Digest ${apiKey(user)}")
            header(CookieAuthenticationFilter.HEADER_CROSS_SITE_COOKIES, "true")
        } When {
            get("v1/authentication/session/${user.publicId}")
        } Then {
            assertObjectResponse()
            body("expiresIn", equalTo(AuthenticationService.APP_VALIDITY.seconds.toInt()))
            cookie(AuthenticationService.SESSION_COOKIE, RestAssuredMatchers.detailedCookie()
                .httpOnly(true)
                .path("/")
                .maxAge(AuthenticationService.WEB_VALIDITY.seconds)
                .secured(false) // tests don't run on https
                .sameSite("none"))
        }
    }

    @Test
    fun getSession_fails_forInvalidApiKey() {
        Given {
            header(HttpHeaders.AUTHORIZATION, "Digest ${UUID.randomUUID()}")
        } When {
            get("v1/authentication/session/${userService.adminUser.publicId}")
        } Then {
            assertErrorResponse(null, HttpStatus.UNAUTHORIZED)
        }
    }

    @Test
    fun deleteSession_works_evenIfNotAuthenticated() {
        When {
            delete("v1/authentication/session")
        } Then {
            assertEmptyResponse()
            cookie(AuthenticationService.SESSION_COOKIE, RestAssuredMatchers.detailedCookie()
                .httpOnly(true)
                .path("/")
                .maxAge(0))
        }
    }
}
