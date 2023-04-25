package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import com.innovattic.medicinfo.logic.dto.triage.ImagesAnswer
import com.innovattic.medicinfo.logic.triage.model.Action
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.Answer
import com.innovattic.medicinfo.logic.triage.model.QuestionDefinition
import com.innovattic.medicinfo.logic.triage.model.QuestionType
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireDefinition
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.util.ResourceUtils

class TriageWithImagesTest : BaseTriageEndpointTest() {

    @BeforeAll
    override fun readSchema() {
        super.readSchema()

        val q2 = createImageQuestion("q2", 1, ActionType.FINISH, nextQuestion = null)
        val q1 = createImageQuestion("q1", 0, nextQuestion = q2)
        // add a test questionnaire with two image questions
        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(TEST_QUESTIONNAIRE, "Test", listOf(q1, q2))
        )
    }

    private fun startTestQuestionnaire(triageStatus: TriageStatus) {
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaAnswer(triageStatus.id, TEST_QUESTIONNAIRE)
    }

    @Test
    fun `Happy flow`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)

        // upload images
        val (img1, bytes1) = uploadImage(token, "test_image_jpeg_1.jpg")
        val (img2) = uploadImage(token, "test_image_jpeg_2.jpg")
        val (img3) = uploadImage(token, "test_image_jpeg_3.jpg")
        val (img4) = uploadImage(token, "test_image_jpeg_4.jpg")
        val (img5) = uploadImage(token, "test_image_jpeg_5.jpg")

        // verify we can download the images
        val img1Data = downloadImage(token, img1)
        assertArrayEquals(bytes1, img1Data)

        // submit question 1
        Given {
            auth().oauth2(token)
        } When {
            val answer = ImagesAnswer("q1", "my answer", listOf(img1, img2))
            body(answer)
            post("v1/triage/answers")
        }

        // submit question 2
        Given {
            auth().oauth2(token)
        } When {
            val answer = ImagesAnswer("q2", "my answer", listOf(img4, img5))
            body(answer)
            post("v1/triage/answers")
        }

        // make sure img3 is removed (was never used in any question)
        assertTrue(triageImageService.exist(label.code, triageStatus.id, img1))
        assertFalse(triageImageService.exist(label.code, triageStatus.id, img3))
        assertTrue(triageImageService.exist(label.code, triageStatus.id, img5))
    }

    @Test
    fun `empty description, two images`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)

        // upload images
        val (img1, bytes1) = uploadImage(token, "test_image_jpeg_1.jpg")
        val (img2) = uploadImage(token, "test_image_jpeg_2.jpg")

        // verify we can download the images
        val img1Data = downloadImage(token, img1)
        assertArrayEquals(bytes1, img1Data)

        Given {
            auth().oauth2(token)
        } When {
            val answer = ImagesAnswer("q1", "", listOf(img1, img2))
            body(answer)
            post("v1/triage/answers")
        } Then {
            body("action", CoreMatchers.equalTo("NEXT"))
            validateAnswerResponse(questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2"))
        }
    }

    @Test
    fun `upload jpeg, png and bmp, throw error on bmp`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)

        // upload images
        val (jpgImgId, jpgImageBytes) = uploadImage(token, "test_image_jpeg_1.jpg")
        val (pngImgId, pngImageBytes) = uploadImage(token, "test_image_png_6.png")
        val (bmpImgId) = uploadImage(token, "test_image_bmp_7.bmp", 400)

        val downloadedJpgData = downloadImage(token, jpgImgId)
        val downloadedPngData = downloadImage(token, pngImgId)

        assertArrayEquals(jpgImageBytes, downloadedJpgData)
        assertArrayEquals(pngImageBytes, downloadedPngData)
        assertNull(bmpImgId)
    }

    @Test
    fun `non-empty description, no images`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)

        Given {
            auth().oauth2(token)
        } When {
            val answer = ImagesAnswer("q1", "hello I am your description", listOf())
            body(answer)
            post("v1/triage/answers")
        } Then {
            body("action", CoreMatchers.equalTo("NEXT"))
            validateAnswerResponse(questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2"))
        }
    }

    @Test
    fun `no description, no images`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)

        Given {
            auth().oauth2(token)
        } When {
            val answer = ImagesAnswer("q1", "", listOf())
            body(answer)
            post("v1/triage/answers")
        } Then {
            body("status", CoreMatchers.equalTo(400))
        }
    }

    @Test
    fun `Cannot answer with unexisting image id`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, label.id, ConversationStatus.OPEN)
        val token = accessToken(user)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        startTestQuestionnaire(triageStatus)

        Given {
            auth().oauth2(token)
        } When {
            val answer = ImagesAnswer("q1", "my answer", listOf("image"))
            body(answer)
            post("v1/triage/answers")
        } Then {
            this.statusCode(HttpStatus.BAD_REQUEST.value())
        }
    }

    private fun uploadImage(token: String, imageResourceName: String, assertedStatusCode: Int = 200, imageContentType: String = "image/jpg"): Pair<String, ByteArray> {
        val file = ResourceUtils.getFile("classpath:test_images/$imageResourceName")

        return Given {
            auth().oauth2(token)
        } When {
            body(file.inputStream())
            contentType(imageContentType)
            post("v1/triage/image")
        } Then {
            statusCode(assertedStatusCode)
        } Extract {
            body().jsonPath().getString("id") to file.readBytes()
        }
    }

    private fun downloadImage(token: String, id: String): ByteArray {
        return Given {
            auth().oauth2(token)
        } When {
            get("v1/triage/image/${id}")
        } Extract {
            body().asByteArray()!!
        }
    }

    private fun createImageQuestion(id: String, pos: Int, action: ActionType = ActionType.GO_TO_QUESTION, nextQuestion: QuestionDefinition?) =
        QuestionDefinition(
            id,
            TEST_QUESTIONNAIRE,
            pos,
            "test",
            id,
            id,
            "",
            "",
            "",
            "",
            QuestionType.DESCRIPTIVE_WITH_PHOTO,
            true,
            false,
            listOf(),
            listOf(
                Answer(
                    0, "test", false, Action(
                        action,
                        0,
                        "",
                        nextQuestion?.uniqueQuestionId ?: "",
                        nextQuestion
                    )
                )
            )
        )
}
