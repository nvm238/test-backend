package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertEmptyResponse
import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import com.innovattic.medicinfo.web.dto.EmailDto
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.ResourceUtils

class EhicEndpointsTest: BaseTriageEndpointTest() {

    @Test
    fun `Valid EHIC image file returns OK`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.setEhicRequiredStatus(triageStatus.id)
        val token = accessToken(user)

        val file = getImage("test_image_jpeg_1.jpg")
        file.inputStream()

        Given {
            auth().oauth2(token)
            contentType(MediaType.IMAGE_JPEG_VALUE)
            body(file.inputStream())
        } When {
            post("v1/ehic/image")
        } Then {
            assertObjectResponse()
            body("ehicValidated", equalTo(true))
        }
    }

    @Test
    fun `ContentType not image returns bad request`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.setEhicRequiredStatus(triageStatus.id)
        val token = accessToken(user)

        val file = getImage("test_image_jpeg_1.jpg")
        file.inputStream()

        Given {
            auth().oauth2(token)
            contentType(MediaType.APPLICATION_PDF_VALUE)
            body(file.inputStream())
        } When {
            post("v1/ehic/image")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Contenttype with unsupported image returns bad request`(){
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.setEhicRequiredStatus(triageStatus.id)
        val token = accessToken(user)

        val file = getImage("test_image_bmp_7.bmp")
        file.inputStream()

        Given {
            auth().oauth2(token)
            contentType(MediaType.IMAGE_GIF_VALUE)
            body(file.inputStream())
        } When {
            post("v1/ehic/image")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Valid email passing results in starting triage`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.setEhicRequiredStatus(triageStatus.id)
        val token = accessToken(user)
        val emailDto = EmailDto("test@validemail.com")

        Given {
            auth().oauth2(token)
            body(emailDto)
        } When {
            post("v1/ehic/email")
        } Then {
            assertEmptyResponse(HttpStatus.OK)
        }
    }

    @Test
    fun `Invalid email returns bad request`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.setEhicRequiredStatus(triageStatus.id)
        val token = accessToken(user)
        val emailDto = EmailDto("invalid-email.com")

        Given {
            auth().oauth2(token)
            body(emailDto)
        } When {
            post("v1/ehic/email")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    private fun getImage(imageResourceName: String) = ResourceUtils.getFile("classpath:test_images/$imageResourceName")
}