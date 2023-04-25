package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertEmptyResponse
import com.innovattic.common.test.assertErrorResponse
import com.innovattic.medicinfo.logic.dto.EmailOptInDto
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.springframework.http.HttpStatus

class EmailOptInEndpointsTest : BaseEndpointTest() {

    @Test
    fun `Send email opt in works for label CZdirect`() {
        val label = getOrCreateLabel("CZdirect")
        val user = createCustomer(label, "c1")
        val dto = EmailOptInDto("email")
        val eloquaApiClient = eloquaApiClients[label.code] ?: throw NoSuchElementException("There is no client for label ${label.code}")

        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/user/${user.publicId}/email-opt-in")
        } Then {
            assertEmptyResponse(HttpStatus.OK)
        }

        Mockito.verify(eloquaApiClient, Mockito.times(1)).sendEmailOptIn(eq("email"))
    }

    @Test
    fun `Send email opt does not trigger for other label`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val dto = EmailOptInDto("email")

        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/user/${user.publicId}/email-opt-in")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `when user publicId is not an UUID, expect not found status`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val dto = EmailOptInDto("email")

        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/user/abc123/email-opt-in")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `when user with given publicId does not exist, expect not found status`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val dto = EmailOptInDto("email")

        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/user/ce421620-74f0-428e-bfe1-0b94d1b0cc34/email-opt-in")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }
}
