package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertListResponse
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetLabelsEndpointTest : BaseEndpointTest() {
    @BeforeEach
    fun reset() {
        messageAttachmentDao.clear()
        labelDao.clear()
    }

    @Test
    fun getLabels_works_whenEmpty() {
        Given {
            auth().oauth2(adminAccessToken)
        } When {
            get("v1/label")
        } Then {
            assertListResponse(0)
        }
    }

    @Test
    fun getLabels_works_withApiKeys() {
        val l1 = createLabel(true)
        val l2 = createLabel()
        Given {
            auth().oauth2(adminAccessToken)
            queryParam("includeApiKeys", true)
        } When {
            get("v1/label")
        } Then {
            assertListResponse(2)
            body("[0].id", equalTo(l1.publicId.toString()))
            body("[0].created", notNullValue())
            body("[0].code", equalTo(l1.code))
            body("[0].name", equalTo(l1.name))
            body("[0].hasPushNotifications", equalTo(true))
            body("[0].fcmApiKey", equalTo(l1.fcmApiKey))
            body("[1].id", equalTo(l2.publicId.toString()))
            body("[1].created", notNullValue())
            body("[1].code", equalTo(l2.code))
            body("[1].name", equalTo(l2.name))
            body("[1].hasPushNotifications", equalTo(false))
            body("[1].fcmApiKey", nullValue())
        }
    }

    @Test
    fun getLabels_doesNotReturnApiKeys_forEmployee() {
        val l1 = createLabel(true)
        Given {
            auth().oauth2(accessToken(createEmployee("e1")))
            queryParam("includeApiKeys", true)
        } When {
            get("v1/label")
        } Then {
            assertListResponse(1)
            body("[0].id", equalTo(l1.publicId.toString()))
            body("[0].created", notNullValue())
            body("[0].code", equalTo(l1.code))
            body("[0].name", equalTo(l1.name))
            body("[0].hasPushNotifications", equalTo(true))
            body("[0].fcmApiKey", nullValue())
        }
    }
}
