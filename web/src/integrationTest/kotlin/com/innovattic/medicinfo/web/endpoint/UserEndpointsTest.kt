package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.database.databaseUtcToZoned
import com.innovattic.common.test.assertEmptyResponse
import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.database.dto.AdminDto
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.CustomerEntryType
import com.innovattic.medicinfo.database.dto.CustomerOnboardingDetailsDto
import com.innovattic.medicinfo.database.dto.EmployeeDto
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.logic.ImageService
import com.innovattic.medicinfo.logic.UserService
import com.innovattic.medicinfo.logic.dto.CreateMessageDto
import com.innovattic.medicinfo.logic.dto.IdDataDto
import com.innovattic.medicinfo.logic.dto.IdType
import com.innovattic.medicinfo.logic.dto.PushNotificationDto
import com.innovattic.medicinfo.logic.dto.UpdateCustomerDto
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

class UserEndpointsTest : BaseEndpointTest() {

    @BeforeEach
    fun beforeEach() {
        clock.reset()
    }

    @AfterEach
    fun afterEach() {
        clock.reset()
    }

    @Test
    fun createAdmin_works() {
        val dto = AdminDto(displayName = "a1", email = "a1@b")
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            post("v1/user/admin")
        } Then {
            assertObjectResponse()
            body("apiKey", notNullValue())
            body("user.id", notNullValue())
            body("user.created", notNullValue())
            body("user.role", equalTo(dto.role.value))
            body("user.displayName", equalTo(dto.displayName))
            body("user.email", equalTo(dto.email))
        }
    }

    @Test
    fun createEmployee_works() {
        val dto = EmployeeDto(displayName = "e1")
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            post("v1/user/employee")
        } Then {
            assertObjectResponse()
            body("apiKey", notNullValue())
            body("user.id", notNullValue())
            body("user.created", notNullValue())
            body("user.role", equalTo(dto.role.value))
            body("user.displayName", equalTo(dto.displayName))
        }
    }

    @Test
    fun createEmployee_fails_forEmployee() {
        val dto = EmployeeDto(displayName = "e2")
        Given {
            auth().oauth2(accessToken(createEmployee("e3")))
            body(dto)
        } When {
            post("v1/user/employee")
        } Then {
            assertErrorResponse(null, HttpStatus.FORBIDDEN)
        }
    }

    @Test
    fun createCustomer_works() {
        val dto = customerDto(createLabel().publicId, "c1")
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            post("v1/user/customer")
        } Then {
            assertObjectResponse()
            body("apiKey", notNullValue())
            body("user.id", notNullValue())
            body("user.created", notNullValue())
            body("user.role", equalTo(dto.role.value))
            body("user.displayName", equalTo(dto.displayName))
            body("user.email", equalTo(dto.email))
            body("user.age", equalTo(dto.age))
            body("user.isInsured", equalTo(dto.isInsured))
            body("user.gender", equalTo(dto.gender?.value))
        }
    }

    @Test
    fun registerCustomer_works() {
        val label = createLabel()
        val dto = customerDto(label.publicId, "c1")
            .copy(
                privacyVersion = "v1",
                privacyVersionAcceptedAt = ZonedDateTime.now(clock),
                phoneNumber = "542123789",
                postalCode = "1234AB",
                houseNumber = "1-A"
            )
        Given {
            header(
                HttpHeaders.AUTHORIZATION,
                "Digest ${UserService.registerCustomerAuthorization(label.publicId, dto.displayName!!)}"
            )
            body(dto)
        } When {
            post("v1/user/customer/register")
        } Then {
            assertObjectResponse()
            body("apiKey", notNullValue())
            body("user.id", notNullValue())
            body("user.created", notNullValue())
            body("user.role", equalTo(dto.role.value))
            body("user.displayName", equalTo(dto.displayName))
            body("user.email", equalTo(dto.email))
            body("user.gender", equalTo(dto.gender?.value))
            body("user.age", equalTo(dto.age))
            body("user.isInsured", equalTo(dto.isInsured))
            body("user.labelId", equalTo(dto.labelId.toString()))
            body("user.privacyVersion", equalTo(dto.privacyVersion))
            body("user.privacyVersionAcceptedAt", notNullValue())
            body("user.birthdate", nullValue())
            body("user.phoneNumber", equalTo(dto.phoneNumber))
            body("user.postalCode", equalTo(dto.postalCode))
            body("user.houseNumber", equalTo(dto.houseNumber))
        }
    }

    @Test
    fun `Register Customer with customer entry details works`() {
        val label = createLabel()
        val customerOnboardingDetails = CustomerOnboardingDetailsDto(
            customerEntryType = CustomerEntryType.GENERAL_PRACTICE,
            generalPractice = "0",
            generalPracticeAGBcode = "1",
            bsn = "123456789"
        )
        val dto = customerDto(label.publicId, "c1")
            .copy(
                customerOnboardingDetails = customerOnboardingDetails
            )
        Given {
            header(
                HttpHeaders.AUTHORIZATION,
                "Digest ${UserService.registerCustomerAuthorization(label.publicId, dto.displayName!!)}"
            )
            body(dto)
        } When {
            post("v1/user/customer/register")
        } Then {
            assertObjectResponse()
            val userId: String = extract().path("user.id")
            val user = userDao.getByPublicId(UUID.fromString(userId))!!
            assertEquals(user.generalPractice, "0")
            assertEquals(user.generalPracticeAgbCode, "1")
            assertEquals(user.entryType, CustomerEntryType.GENERAL_PRACTICE.salesforceTranslation)
        }

        Mockito.verify(salesforceService, Mockito.atLeastOnce()).sendOnboardingData(
            any(),
            eq(customerOnboardingDetails),
            anyOrNull()
        )
    }



    @Test
    fun `Register Customer with invalid customer entry details bsn does not work`() {
        val label = createLabel()
        val customerOnboardingDetails = CustomerOnboardingDetailsDto(
            customerEntryType = CustomerEntryType.GENERAL_PRACTICE,
            bsn = "invalid"
        )
        val dto = customerDto(label.publicId, "c1")
            .copy(
                privacyVersion = "v1",
                privacyVersionAcceptedAt = ZonedDateTime.now(clock),
                birthdate = ZonedDateTime.now(clock),
                customerOnboardingDetails = customerOnboardingDetails
            )
        Given {
            header(
                HttpHeaders.AUTHORIZATION,
                "Digest ${UserService.registerCustomerAuthorization(label.publicId, dto.displayName!!)}"
            )
            body(dto)
        } When {
            post("v1/user/customer/register")
        } Then {
            assertErrorResponse(null, status = HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun registerCustomer_fails_whenInvalidAuthorization() {
        val dto = customerDto(createLabel().publicId, "invalid")
        Given {
            header(
                HttpHeaders.AUTHORIZATION,
                "Digest ${UserService.registerCustomerAuthorization(UUID.randomUUID(), dto.displayName!!)}"
            )
            body(dto)
        } When {
            post("v1/user/customer/register")
        } Then {
            assertErrorResponse(null, HttpStatus.UNAUTHORIZED)
        }
    }

    @Test
    fun updateCustomer_works() {
        val label = createLabel(withPushNotifications = true)
        val user = createCustomer(label, "c1")
        val dto = UpdateCustomerDto(
            displayName = "c1 updated",
            gender = Gender.MALE,
            email = "c1updated@innovattic.com",
            age = 32,
            isInsured = true,
            deviceToken = "112233"
        )
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            patch("v1/user/me")
        } Then {
            assertObjectResponse()
            body("id", equalTo(user.publicId.toString()))
            body("displayName", equalTo("c1 updated"))
            body("deviceToken", equalTo(dto.deviceToken))
        }

        val updatedUser = userDao.getById(user.id)!!
        assertEquals(user.labelId, updatedUser.labelId)
        assertEquals(dto.age, updatedUser.age)
        assertEquals(dto.email, updatedUser.email)
        assertEquals(dto.gender, updatedUser.gender)
        assertEquals(dto.isInsured, updatedUser.isInsured)
        assertEquals(dto.deviceToken, updatedUser.deviceToken)
    }

    @Test
    fun updateCustomerPrivacySettings_works() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val acceptedAtTime = LocalDateTime.of(2022, 6, 7, 16, 0, 0, 0)
        clock.setTime(acceptedAtTime)
        val dto = UpdateCustomerDto(privacyVersion = "v1")
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            patch("v1/user/me")
        } Then {
            assertObjectResponse()
            body("id", equalTo(user.publicId.toString()))
        }

        val updatedUser = userDao.getById(user.id)!!
        val latestPrivacyStatementEntry = privacyStatementDao.findLatestByUser(user.id)
            ?: throw IllegalStateException("privacy statement should be present")
        assertEquals(user.labelId, updatedUser.labelId)
        assertEquals(dto.privacyVersion, latestPrivacyStatementEntry.version)
        assertEquals(acceptedAtTime, latestPrivacyStatementEntry.acceptedAt)
    }

    @Test
    fun `given accpeted privacy statement, when customer accepts new version, expect latest version returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val acceptedAtTimeV1 = LocalDateTime.of(2022, 6, 7, 16, 0, 0, 0)
        clock.setTime(acceptedAtTimeV1)
        privacyStatementDao.getExistingOrCreate(user.id, "v1")
        val acceptedAtTimeV2 = LocalDateTime.of(2022, 6, 7, 17, 0, 0, 0)
        clock.setTime(acceptedAtTimeV2)
        val dto = UpdateCustomerDto(privacyVersion = "v2")
        var responseCustomerDto: CustomerDto? = null
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            patch("v1/user/me")
        } Then {
            assertObjectResponse()
            responseCustomerDto = extract().body().`as`(CustomerDto::class.java)
            body("id", equalTo(user.publicId.toString()))
        }

        if (responseCustomerDto == null) throw IllegalStateException("Response object is null!")
        assertEquals(label.publicId, responseCustomerDto!!.labelId)
        assertEquals(dto.privacyVersion, responseCustomerDto!!.privacyVersion)
        assertEquals(databaseUtcToZoned(acceptedAtTimeV2), responseCustomerDto!!.privacyVersionAcceptedAt)

        // privacy statement v1 also present in database
        val privacyStatementV1 = privacyStatementDao.findByUserAndVersion(user.id, "v1")
            ?: throw IllegalStateException("privacy statement should be present")
        assertEquals("v1", privacyStatementV1.version)
        assertEquals(acceptedAtTimeV1, privacyStatementV1.acceptedAt)
    }

    @Test
    fun updateCustomer_fails_forCustomer() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val dto = CustomerDto(displayName = "c1 updated", labelId = label.publicId)
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            put("v1/user/${user.publicId}")
        } Then {
            assertErrorResponse(null, status = HttpStatus.FORBIDDEN)
        }
    }

    @Test
    fun updateCustomer_works_forAdmin() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val dto = CustomerDto(
            displayName = "c1 updated",
            labelId = label.publicId,
            gender = Gender.MALE,
            email = "c1updated@innovattic.com",
            age = 32,
            isInsured = true
        )
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            put("v1/user/${user.publicId}")
        } Then {
            assertObjectResponse()
            body("id", equalTo(user.publicId.toString()))
            body("displayName", equalTo("c1 updated"))
        }

        val updatedUser = userDao.getById(user.id)!!
        assertEquals(user.labelId, updatedUser.labelId)
        assertEquals(dto.age, updatedUser.age)
        assertEquals(dto.email, updatedUser.email)
        assertEquals(dto.gender, updatedUser.gender)
        assertEquals(dto.isInsured, updatedUser.isInsured)
    }

    @Test
    fun deleteUser_works_forAdmin() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val conversationPojo = conversationDao.get(conversation.id)!!
        messageService.create(user, conversation.id, CreateMessageDto(message = "Test message"))
        Given {
            auth().oauth2(adminAccessToken)
        } When {
            delete("v1/user/${user.publicId}")
        } Then {
            assertEmptyResponse()
        }
        assertNull(userDao.getById(user.id))
        assertNotNull(labelDao.getById(label.id)) { "User delete also deleted label" }
        assertNull(conversationDao.get(conversation.id))
        assertEquals(0, messageDao.get(conversationPojo.id, null, null).size)
    }

    @Test
    fun deleteUserAttachments() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val conversationPojo = conversationDao.get(conversation.id)!!
        messageService.create(user, conversation.id, CreateMessageDto(message = "Test message"))
        val message = "This is a message"
        val createWithAttachment = messageService.createWithAttachment(
            user,
            conversation.id,
            "filename",
            "image/png",
            ClassPathResource("innovattic-logo-dark.png").inputStream,
            message
        )
        val attachment = findMessageAttachment(conversationPojo, createWithAttachment.id!!)
        Given {
            auth().oauth2(adminAccessToken)
        } When {
            delete("v1/user/${user.publicId}/attachments")
        } Then {
            assertEmptyResponse()
        }
        assertNotNull(userDao.getById(user.id))
        assertNotNull(labelDao.getById(label.id)) { "User delete also deleted label" }
        val conversationDto = messageService.get(user, conversation.id, null, null)
        conversationDto.messages.forEach { assertNull(it.attachment) }
        assertThrows<FileNotFoundException> { fileService.readFile(ImageService.imageKey(attachment.publicId, label.code)) }
        assertEquals(2, messageDao.get(conversationPojo.id, null, null).size)
    }

    @Test
    fun deleteSingleUserAttachment() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val conversationPojo = conversationDao.get(conversation.id)!!
        messageService.create(user, conversation.id, CreateMessageDto(message = "Test message"))
        val message = "This is a message"
        val messageWithAttachmentToDelete = messageService.createWithAttachment(
            user,
            conversation.id,
            "filename",
            "image/png",
            ClassPathResource("innovattic-logo-dark.png").inputStream,
            message
        )
        val messageWithAttachmentToStay = messageService.createWithAttachment(
            user,
            conversation.id,
            "filename",
            "image/png",
            ClassPathResource("innovattic-logo-dark.png").inputStream,
            message
        )
        val attachmentToDelete = findMessageAttachment(conversationPojo, messageWithAttachmentToDelete.id!!)

        Given {
            auth().oauth2(adminAccessToken)
        } When {
            delete("v1/user/${user.publicId}/attachments/${attachmentToDelete.publicId}")
        } Then {
            assertEmptyResponse()
        }
        assertNotNull(userDao.getById(user.id))
        assertNotNull(labelDao.getById(label.id)) { "User delete also deleted label" }
        val conversationDto = messageService.get(user, conversation.id, null, null)
        val messageWithAttachmentLeft = conversationDto.messages.singleOrNull { it.attachment != null }
            ?: throw IllegalStateException("Should not be null! This has to have one element")
        assertEquals(messageWithAttachmentToStay.id, messageWithAttachmentLeft.id)
        assertEquals(3, messageDao.get(conversationPojo.id, null, null).size)
    }

    @Test
    fun deleteUser_fails_forOwnUser() {
        val user = createEmployee("e1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            delete("v1/user/${user.publicId}")
        } Then {
            assertErrorResponse(null, HttpStatus.FORBIDDEN)
        }
        assertNotNull(userDao.getById(user.id))
    }

    @Test
    fun sendIdData_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        conversationService.create(user)
        Given {
            auth().oauth2(accessToken(user))
            body(IdDataDto(IdType.DRIVERS_LICENSE, "1234", "Jan", "Jansen", "123456789", ZonedDateTime.now()))
        } When {
            post("v1/user/customer/${user.publicId}/confirm-id-data")
        } Then {
            assertEmptyResponse()
        }
    }

    @Test
    fun sendIdData_works_withOnlyIdTypeAndIdNumber() {
        val user = createCustomer(createLabel(), "c1")
        conversationService.create(user)
        Given {
            auth().oauth2(accessToken(user))
            body(IdDataDto(IdType.DRIVERS_LICENSE, "1234"))
        } When {
            post("v1/user/customer/${user.publicId}/confirm-id-data")
        } Then {
            assertEmptyResponse()
        }
    }

    @Test
    fun sendIdData_fails_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        Given {
            auth().oauth2(accessToken(user))
            body(IdDataDto(IdType.DRIVERS_LICENSE, "1234", "Jan", "Jansen", "123456789", ZonedDateTime.now()))
        } When {
            post("v1/user/customer/${user.publicId}/confirm-id-data")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun sendPushNotification_works_forAdminToCustomer() {
        val target = createCustomer(createLabel(), "c2", configurePushNotifications = true)
        Given {
            auth().oauth2(adminAccessToken)
            body(PushNotificationDto("Title", "Message", mapOf("key" to "value")))
        } When {
            post("v1/user/customer/${target.publicId}/send-push-notification")
        } Then {
            assertEmptyResponse()
        }
    }

    @Test
    fun createCustomerWithDuplicateDeviceToken_works() {
        val label = createLabel(withPushNotifications = true)
        val user = createCustomer(label, "c1", configurePushNotifications = true)
        val dto = customerDto(label.publicId, "c1")
            .copy(
                privacyVersion = "v1",
                privacyVersionAcceptedAt = ZonedDateTime.now(clock),
                deviceToken = "test-token"
            )
        Given {
            header(
                HttpHeaders.AUTHORIZATION,
                "Digest ${UserService.registerCustomerAuthorization(label.publicId, dto.displayName!!)}"
            )
            body(dto)
        } When {
            post("v1/user/customer/register")
        } Then {
            assertObjectResponse()
        }

        val oldUser = userDao.getById(user.id)!!
        assertEquals(null, oldUser.deviceToken)
        assertEquals(null, oldUser.snsEndpointArn)
    }

    @Test
    fun updateCustomerWithDuplicateDeviceToken_works() {
        val label = createLabel(withPushNotifications = true)

        val firstUser = createCustomer(label, "c1", configurePushNotifications = true)
        val secondUser = createCustomer(label, "c2", configurePushNotifications = true)
            .apply {
                userDao.registerDeviceToken(this.id, "test_token_2", "test_arn_2")
            }

        val dto = CustomerDto(
            displayName = "c2 updated",
            labelId = label.publicId,
            gender = Gender.MALE,
            email = "c2updated@innovattic.com",
            age = 32,
            isInsured = true,
            deviceToken = "test-token"
        )
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            put("v1/user/${secondUser.publicId}")
        } Then {
            assertObjectResponse()
            body("deviceToken", equalTo("test-token"))
        }

        val oldUser = userDao.getById(firstUser.id)!!
        assertEquals(null, oldUser.deviceToken)
        assertEquals(null, oldUser.snsEndpointArn)
    }

    @Test
    fun registerCustomerWhoIsUkrainianRefugeeWorks() {
        val label = createLabel()
        val customerOnboardingDetailsDto = CustomerOnboardingDetailsDto(
            customerEntryType = CustomerEntryType.UKRAINIAN_REFUGEE,
            shelterLocationId = "1",
            shelterLocationName = "The first shelter location"
        )
        val dto = customerDto(label.publicId, "c1")
            .copy(customerOnboardingDetails = customerOnboardingDetailsDto)

        Given {
            header(
                HttpHeaders.AUTHORIZATION,
                "Digest ${UserService.registerCustomerAuthorization(label.publicId, dto.displayName!!)}"
            )
            body(dto)
        } When {
            post("v1/user/customer/register")
        } Then {
            assertObjectResponse()
            val userId: String = extract().path("user.id")
            val user = userDao.getByPublicId(UUID.fromString(userId))!!
            assertEquals(user.entryType, CustomerEntryType.UKRAINIAN_REFUGEE.salesforceTranslation)
            assertEquals(user.shelterLocationId, "1")
            assertEquals(user.shelterLocationName, "The first shelter location")
        }

    }

}
