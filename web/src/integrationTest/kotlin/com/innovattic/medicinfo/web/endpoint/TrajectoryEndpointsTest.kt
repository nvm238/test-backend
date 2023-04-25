package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertEmptyResponse
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test

class TrajectoryEndpointsTest : BaseEndpointTest() {
    @Test
    fun close_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        conversationService.create(user)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/trajectory/close")
        } Then {
            assertEmptyResponse()
        }
    }
}
