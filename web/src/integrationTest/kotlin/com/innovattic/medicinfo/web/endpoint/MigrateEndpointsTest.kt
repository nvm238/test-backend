package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.logic.dto.migration.MigrateDto
import com.innovattic.medicinfo.web.BaseEndpointTest
import com.innovattic.medicinfo.web.endpoint.v1.MigrationEndpoint
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class MigrateEndpointsTest : BaseEndpointTest() {

    @Test
    fun `Migrate customer fails for non existent user`() {
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val dto = MigrateDto(
            userId,
            conversationId
        )
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Migrate customer fails for invalid user and different conversation combo`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        conversationService.create(user)
        val dto = MigrateDto(
            user.publicId,
            UUID.randomUUID()
        )
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Migrate customer fails for invalid user and no conversation`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val dto = MigrateDto(
            user.publicId,
            UUID.randomUUID()
        )
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Migrate customer with existing api key fails for user`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val dto = MigrateDto(
            user.publicId,
            conversation.id
        )
        userDao.generateApiKey(user.id)
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Migrate customer with existing api key succeeds with testing header`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val dto = MigrateDto(
            user.publicId,
            conversation.id
        )
        userDao.generateApiKey(user.id)
        Given {
            header(MigrationEndpoint.HEADER_IGNORE_EXISTING_APIKEY, "true")
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertObjectResponse()
        }
    }

    @Test
    fun `Migrate customer works for user`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val dto = MigrateDto(
            user.publicId,
            conversation.id
        )
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertObjectResponse()
            body("userId", equalTo(user.publicId.toString()))
            body("conversationId", equalTo(conversation.id.toString()))
            body("apiKey", notNullValue())
        }
    }

    @Test
    fun `Migrate customer fails for user which is an employee and chat id is from valid customer`() {
        val label = createLabel()
        val user = createEmployee("e1")
        val customer = createCustomer(label, "c1")
        val conversation = conversationService.create(customer)
        val dto = MigrateDto(
            user.publicId,
            conversation.id
        )
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Migrate customer fails for user which is an employee and chat id is random`() {
        val user = createEmployee("e1")
        val dto = MigrateDto(
            user.publicId,
            UUID.randomUUID()
        )
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Migrate customer fails for user which is an employee and chat id is null`() {
        val user = createEmployee("e1")
        val dto = MigrateDto(
            user.publicId,
            null
        )
        Given {
            body(dto)
        } When {
            post("v1/migrate")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }
}
