package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.CustomerEntryType
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.logic.dto.salesforce.UserContactDataSalesforceResponse
import com.innovattic.medicinfo.logic.dto.triage.StopReason
import com.innovattic.medicinfo.logic.dto.triage.TriageState
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class TriageStartEndpointTest : BaseTriageEndpointTest() {

    @BeforeEach
    fun beforeEach() {
        doReturn(questionSchemaService.getQuestionSchema(0)).`when`(questionSchemaService).getLatestSchema()
    }

    @Test
    fun `given no conversation, expect new triage started and first profile question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")

        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
            body("conversation.id", not(equalTo(null)))
            body("conversation.status", equalTo("open"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }
    }

    @Test
    @DisplayName(
        """
        given no conversation and user without birthday,
         when calling start endpoint and salesforce responds with date,
          expect birthday synchronized and new triage started and first profile question returned
    """
    )
    fun `test with birthday in salesforce`() {
        val label = createLabel()
        val user = createCustomer(label, "c1", age = null)
        doAnswer {
            UserContactDataSalesforceResponse(LocalDate.of(2000, 3, 1), false, user.publicId)
        }.`when`(salesforceService).getCustomerContactData(user.publicId)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
            body("conversation.id", not(equalTo(null)))
            body("conversation.status", equalTo("open"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }
    }

    @Test
    @DisplayName(
        """
        given no conversation and user NOT marked as inactive in Salesforce,
         when calling start endpoint and salesforce responds with data and customer onboarding data,
          expect user to be updated with customer onboarding data
    """
    )
    fun `test with customer onboarding data in salesforce`() {
        val label = createLabel()
        val user = createCustomer(label, "c1", age = null)

        doAnswer {
            UserContactDataSalesforceResponse(
                LocalDate.of(2000, 3, 1),
                false,
                user.publicId,
                CustomerEntryType.GENERAL_PRACTICE_CENTER.salesforceTranslation,
                null, null,
                "ABC", "DEF",
                null
            )
        }.`when`(salesforceService).getCustomerContactData(user.publicId)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
            val dbUser = userDao.getById(user.id)!!
            assertEquals(dbUser.generalPracticeCenter, "ABC")
            assertEquals(dbUser.generalPracticeCenterAgbCode, "DEF")
            assertEquals(dbUser.entryType, CustomerEntryType.GENERAL_PRACTICE_CENTER.salesforceTranslation)
        }
    }

    @Test
    @DisplayName(
        """
        given no conversation and user marked as inactive in Salesforce,
         when calling start endpoint and salesforce responds with data,
          expect triage FINISHED returned
    """
    )
    fun `test with inactive user in salesforce`() {
        val label = getOrCreateLabel("SFINACTIVETEST")
        val user = createCustomer(label, "c1", age = null)
        var returnedConversationId: String? = null
        doAnswer {
            UserContactDataSalesforceResponse(LocalDate.of(2000, 3, 1), true, user.publicId)
        }.`when`(salesforceService).getCustomerContactData(user.publicId)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            returnedConversationId = extract().path("conversation.id")
            body("triageStatus", equalTo("PERMANENTLY_CLOSED"))
            body("conversation.id", not(equalTo(null)))
            body("conversation.status", equalTo("open"))
        }

        val conversation = conversationDao.get(UUID.fromString(returnedConversationId))!!
        val triageStatus = triageStatusDao.getByConversationId(conversation.id)!!
        assertEquals(TriageProgress.NOT_APPLICABLE, triageStatus.status)
    }

    @Test
    @DisplayName(
        """
        given no conversation and user without birthday,
         when calling start endpoint and salesforce responds with error,
          expect birthday 500
    """
    )
    fun `test with no birthday in salesforce`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        doThrow(IllegalStateException("error")).`when`(salesforceService).getCustomerContactData(user.publicId)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("status", equalTo(500))
        }
    }

    @Test
    @DisplayName(
        """
        given no conversation and user without birthday but with age and require age feature enabled in properties,
         when calling start endpoint,
          expect new triage started and first profile question returned
    """
    )
    fun `test with age and age feature enabled`() {
        val label = getOrCreateLabel("CZ")
        val user = createCustomer(label, "c1", age = 25)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
            body("conversation.id", not(equalTo(null)))
            body("conversation.status", equalTo("open"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }
    }

    @Test
    @DisplayName(
        """
        given no conversation and user without birthday but with age and require age feature disabled in properties,
         when calling start endpoint,
          expect new triage started and first profile question returned
    """
    )
    fun `test with age and age feature disabled`() {
        val label = createLabel()
        val user = createCustomer(label, "c1", age = 25)
        doAnswer {
            UserContactDataSalesforceResponse(null, false, user.publicId)
        }.`when`(salesforceService).getCustomerContactData(user.publicId)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
            body("conversation.id", not(equalTo(null)))
            body("conversation.status", equalTo("open"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }
    }

    @Test
    fun `given started triage without questions answered, expect first profile question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }
    }

    @Test
    fun `given started conversation with messages, but without triage, expect finished returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        messageDao.create(user.id, conversation.id, "hey", null, null)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
        }
    }

    @Test
    fun `given archived conversation, expect new triage started and first profile quesiton returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.ARCHIVED)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        val firstProfileQuestion = questionnaireModel.whoProfileQuestion
        saveAnswer(firstProfileQuestion, triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }
    }

    @Test
    fun `given open conversation and triage in progress with first question answered, expect medical area quesiton returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        val firstProfileQuestion = questionnaireModel.whoProfileQuestion
        saveAnswer(firstProfileQuestion, triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("IN_PROGRESS"))
            vaildateFemaleMedareaAnswers(true, startTriageQuestionObjectName)
        }
    }

    //  https://innovattic.atlassian.net/browse/MEDSLA-221
    @Test
    fun `given stale open conversation and finished triage with not loaded version, expect triage FINISHED state returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, 99999, conversation.id)
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
            body("conversation.id", equalTo(conversation.publicId.toString()))
            body("conversation.status", equalTo("open"))
            body("question", equalTo(null))
        }
    }

    @Test
    fun `given open conversation and finished triage, expect triage FINISHED state returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
            body("conversation.id", equalTo(conversation.publicId.toString()))
            body("conversation.status", equalTo("open"))
            body("question", equalTo(null))
        }
    }

    @Test
    fun `given open conversation and triage finished by go_to_chat, expect triage FINISHED state returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_CHAT)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
            body("conversation.id", equalTo(conversation.publicId.toString()))
            body("conversation.status", equalTo("open"))
            body("question", equalTo(null))
        }
    }

    @Test
    fun `given open conversation and abandoned triage, expect triage FINISHED state returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_USER_WITH_CHAT)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
            body("conversation.id", equalTo(conversation.publicId.toString()))
            body("conversation.status", equalTo("open"))
            body("question", equalTo(null))
        }
    }

    @Test
    @DisplayName(
        """
        given open conversation and abandoned triage on medical area question,
         when calling endpoint with medical area parameter equal to KEELK,
          expect triage continued and first question of chosen medical area returned
    """
    )
    fun `given open conversation and abandoned triage on medarea, when call with medical area param, expect triage continue`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_USER_WITH_CHAT)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage?medicalArea=KEELK")
        } Then {
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion("KEELK"))
        }

        triageStatus = triageStatusDao.getById(triageStatus.id) ?: throw IllegalStateException("Triage is null!")
        assertEquals(TriageProgress.CONTINUED_AFTER_STOP, triageStatus.status)
    }


    @Test
    @DisplayName(
        """
        given finished triage on OVERIG question area,
         when calling endpoint with medical area parameter equal to KEELK,
          expect triage continued and first question of chosen medical area returned
    """
    )
    fun `given finished triage on OVERI question area, when call with medical area param, expect triage continue`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)

        // Finish the questionnaire in the "OVERIG" medical area
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaOVERIGAnswer(triageStatus.id)
        saveAnswer(questionnaireModel.getQuestionByUniqueId("OVERIG", "OVERI1"), triageStatus.id, answer = "hello")
        saveAnswer(
            questionnaireModel.getQuestionByUniqueId("OVERIG", "OVERI-ADDITIONALQ"),
            triageStatus.id, answer = "world"
        )
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_USER_OTHER)

        // Update time to properly save the new medical area as the latest answer.
        clock.plusTime(amount = 5, ChronoUnit.MINUTES)

        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage?medicalArea=KEELK")
        } Then {
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion("KEELK"))
        }

        triageStatus = triageStatusDao.getById(triageStatus.id) ?: throw IllegalStateException("Triage is null!")
        assertEquals(TriageProgress.CONTINUED_AFTER_STOP, triageStatus.status)
    }

    @Test
    fun `given open conversation and abandoned triage on medarea, when call without param, expect triage finished`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_USER_WITH_CHAT)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
            body("conversation.id", equalTo(conversation.publicId.toString()))
            body("conversation.status", equalTo("open"))
            body("question", equalTo(null))
        }
    }

    @Test
    fun `given triage with first profile question answered for others, expect second profile question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(questionnaireModel.whoProfileQuestion.answer[1].action.nextQuestion!!)
        }
    }

    @Test
    fun `given triage with first profile question answered for myself, expect medical area question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        val firstProfileQuestion = questionnaireModel.whoProfileQuestion
        saveAnswer(firstProfileQuestion, triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("IN_PROGRESS"))
            vaildateFemaleMedareaAnswers(true, startTriageQuestionObjectName)
        }
    }

    @Test
    fun `given triage with first profile question answered for myself and medical area KEELK, expect first KEELK question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            val keelkQuestion = questionnaireModel.findFirstQuestion("KEELK")
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(keelkQuestion)
        }
    }

    @Test
    @DisplayName(
        """given triage with first profile question answered for others and medical area KEELK,
        expect first KEELK question returned with question asked for caregiver"""
    )
    fun `expect KEELK and question for caregiver`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        answerProfileQuestionsWithoutMedicalArea(triageStatus.id)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(keelk1Question, false)
        }
    }

    @Test
    fun `given triage with KEELK9 answered Ja, expect KEELK9a question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk9Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9")
        saveAnswer(keelk9Question, triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            val keelk9aQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9A")
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(keelk9aQuestion)
        }
    }

    @Test
    fun `given triage with KEELK9 answered Nee, expect KEELK6 question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk9Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9")
        saveAnswer(keelk9Question, triageStatus.id, 2)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            val keelk6Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6")
            body("triageStatus", equalTo("IN_PROGRESS"))
            validateStartTriageResponse(keelk6Question)
        }
    }

    @Test
    fun `given no triage, when starting triage with lang parameter with value nl, expect first profile question`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage?lang=nl")
        } Then {
            body("triageStatus", equalTo("NEW"))
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }

        val conversation = conversationDao.getLatestOpen(user.id) ?: throw IllegalStateException("No open conversation")
        val triageStatus = triageStatusDao.getByConversationId(conversation.id)
            ?: throw IllegalStateException("No triage present for conversation")

        assertEquals(TriageProgress.STARTED, triageStatus.status)
        assertEquals(conversation.language, "nl")
    }

    @Test
    fun `given no triage, when starting triage with invalid lang parameter with value cp, bad request`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage?lang=cp")
        } Then {
            body("status", equalTo(400))
        }
    }

    @Test
    fun `given no triage, when starting triage with invalid lang parameter with value en, expect triage finished response`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage?lang=zu")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
            body("conversation.status", equalTo("open"))
            body("question", equalTo(null))
        }

        val conversation = conversationDao.getLatestOpen(user.id) ?: throw IllegalStateException("No open conversation")
        val triageStatus = triageStatusDao.getByConversationId(conversation.id)
            ?: throw IllegalStateException("No triage present for conversation")

        assertEquals(TriageProgress.NOT_APPLICABLE, triageStatus.status)
        assertEquals(conversation.language, "zu")
    }

    @Test
    fun `restarting a stopped triage by the nurse will return next question where user stopped the triage`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.endTriageStatus(
            triageStatus.id,
            TriageProgress.FINISHED_BY_USER_WITH_CHAT,
            StopReason.WANTS_CHAT.name
        )

        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/continue")
        } Then {
            assertObjectResponse()
            validateStartTriageResponse(questionnaireModel.findFirstQuestion(PROFILE_QUESTIONNAIRE_ID))
        }
    }


    @Test
    fun `restarting a non existing triage will return conversation missing error`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage/continue")
        } Then {
            assertErrorResponse(ErrorCodes.CONVERSATION_MISSING, HttpStatus.NOT_FOUND, errorCodeField = "errorCode")
        }
    }

    @Test
    fun `given holiday tourist from abroad get EHIC required Status`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        userDao.updateCustomersOnboardingDetails(
            user.id,
            CustomerEntryType.HOLIDAY_FOREIGN_TOURIST.salesforceTranslation,
            null,
            null,
            null,
            null,
            "Amsterdam",
            null,
            null,
        )
        doAnswer {
            UserContactDataSalesforceResponse(
                null,
                false,
                UUID.randomUUID(),
                CustomerEntryType.HOLIDAY_FOREIGN_TOURIST.salesforceTranslation
            )
        }.`when`(salesforceService).getCustomerContactData(any())
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            assertObjectResponse()
            body("triageStatus", equalTo(TriageState.EHIC_REQUIRED.name))
        }
    }

    @Test
    fun `given user closed app while in EHIC_REQUIRED state still gets EHIC REQUIRED status`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        userDao.updateCustomersOnboardingDetails(
            user.id,
            CustomerEntryType.HOLIDAY_FOREIGN_TOURIST.salesforceTranslation,
            null,
            null,
            null,
            null,
            "Amsterdam",
            null,
            null,
        )
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        triageStatusDao.setEhicRequiredStatus(triageStatus.id)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            assertObjectResponse()
            body("triageStatus", equalTo(TriageState.EHIC_REQUIRED.name))
        }
    }
}
