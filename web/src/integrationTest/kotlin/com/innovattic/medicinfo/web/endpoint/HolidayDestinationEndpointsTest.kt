package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertListResponse
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class HolidayDestinationEndpointsTest : BaseEndpointTest() {

    @Test
    fun `Get holiday destinations works for label`() {
        val label = createLabel()

        Given {
            queryParam("labelCode", label.code)
        } When {
            get("v1/holiday-destination")
        } Then {
            assertListResponse(1)
            body("[0].id", equalTo("0"))
            body("[0].name", equalTo("holiday destination"))
        }
    }

    @Test
    fun `Get general practices fails with unknown label`() {
        Given {
            queryParam("labelCode", "AAA")
        } When {
            get("v1/holiday-destination")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Get general practices fails with no label`() {
        When {
            get("v1/holiday-destination")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }
}
