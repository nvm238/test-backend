package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertListResponse
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class ShelterLocationEndpointTest: BaseEndpointTest() {

    @Test
    fun `Get shelter locations`() {
        When {
            get("v1/shelter-location")
        } Then {
            assertListResponse(2)
            body("[0].id", equalTo("0"))
            body("[0].name", equalTo("Shelter location number 1"))
            body("[1].id", equalTo("1"))
            body("[1].name", equalTo("Shelter location number 2"))
        }
    }
}
