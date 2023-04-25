package com.innovattic.medicinfo.web

import com.innovattic.medicinfo.web.integration.mirro.MirroTokenService
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class MirroTokenServiceTest : BaseEndpointTest() {

    @Autowired
    lateinit var mirroTokenService: MirroTokenService

    @Test
    fun generateSignedJWT() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val accessToken = mirroTokenService.createAccessToken(clock.instant(), user, 2, Duration.ofMinutes(15))

        assertNotNull(mirroTokenService.verifyJwt(accessToken))
    }

    @Test
    fun getMirroLinkWithToken() {
        val label = createLabel()
        val user = createCustomer(label, "c1")

        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/mirro/login/2")
        } Then {
            val constantUrlPart = "https://dummy-url.com/"
            val url: String = extract().path("url")
            assertTrue(url.startsWith(constantUrlPart), "$url does not start with $constantUrlPart")
            val jwt = url.substring(constantUrlPart.length)
            assertNotNull(mirroTokenService.verifyJwt(jwt))
        }
    }
}