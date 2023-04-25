package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertListResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.web.BaseEndpointTest
import com.innovattic.medicinfo.web.security.AuthenticationService
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class GetUserEndpointsTest : BaseEndpointTest() {
    @BeforeEach
    fun reset() {
        messageAttachmentDao.clear()
        clearAllUsersExceptMainAdmin()
    }

    @Test
    fun getUsers_works_whenNoResults() {
        Given {
            auth().oauth2(adminAccessToken)
            queryParam("query", "role!=admin")
        } When {
            get("v1/user")
        } Then {
            assertListResponse(0)
        }
    }

    @Test
    fun getUsers_works_forMixedTypes() {
        val c1 = createCustomer(createLabel(), "c1")
        val e1 = createEmployee("e1")
        val c2 = createCustomer(createLabel(), "c2")
        val e2 = createEmployee("e2")
        Given {
            auth().oauth2(adminAccessToken)
        } When {
            get("v1/user")
        } Then {
            assertListResponse(5)
            assertUser("[0].", userService.adminUser)
            assertUser("[1].", c1)
            assertUser("[2].", e1)
            assertUser("[3].", c2)
            assertUser("[4].", e2)
        }
    }

    @Test
    fun getUsers_works_withQueryAndOrder() {
        val l1 = createLabel()
        val c4 = createCustomer(l1, "c4")
        val c3 = createCustomer(l1, "c3", insured = false)
        val c2 = createCustomer(l1, "c2", age = 32)
        val c1 = createCustomer(l1, "c1", Gender.MALE)
        createEmployee("e1")
        Given {
            auth().oauth2(adminAccessToken)
            queryParam("query", "role==customer;labelId==${l1.publicId}")
            queryParam("order", "role", "displayName", "age")
        } When {
            get("v1/user")
        } Then {
            assertListResponse(4)
            assertUser("[0].", c1)
            assertUser("[1].", c2)
            assertUser("[2].", c3)
            assertUser("[3].", c4)
        }
    }

    @Test
    fun getMe_works_forAdmin() {
        Given {
            auth().oauth2(adminAccessToken)
        } When {
            get("v1/user/me")
        } Then {
            assertObjectResponse()
            assertUser("", userService.adminUser)
        }
    }

    @Test
    fun getMe_works_forEmployee() {
        val user = createEmployee("e1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/user/me")
        } Then {
            assertObjectResponse()
            assertUser("", user)
        }
    }

    @Test
    fun getMe_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/user/me")
        } Then {
            assertObjectResponse()
            assertUser("", user)
        }
    }

    @Test
    fun getMe_works_forCookieAuth() {
        val user = createCustomer(createLabel(), "c2")
        Given {
            header(HttpHeaders.COOKIE, "${AuthenticationService.SESSION_COOKIE}=${accessToken(user)}")
        } When {
            get("v1/user/me")
        } Then {
            assertObjectResponse()
            assertUser("", user)
        }
    }
}
