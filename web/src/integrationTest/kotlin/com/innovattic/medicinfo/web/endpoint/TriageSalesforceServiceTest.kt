package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.logic.TriageImageService
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAnswersRequestDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAttachmentData
import com.innovattic.medicinfo.logic.dto.triage.DateAnswer
import com.innovattic.medicinfo.logic.dto.triage.ImagesAnswer
import com.innovattic.medicinfo.logic.dto.triage.SkipAnswer
import com.innovattic.medicinfo.logic.dto.triage.UncertainAnswer
import com.innovattic.medicinfo.logic.triage.CATEGORY_OTHER_COMPLAINT
import com.innovattic.medicinfo.logic.triage.UNKNOWN_URGENCY
import com.innovattic.medicinfo.logic.triage.model.ADDITIONAL_INFO_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.BIRTHDAY_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.BSN_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.DescriptiveWithImages
import com.innovattic.medicinfo.logic.triage.model.FIRSTNAME_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.LASTNAME_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireDefinition
import com.innovattic.medicinfo.logic.triage.model.RELATION_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import java.io.ByteArrayInputStream
import java.time.LocalDate

class TriageSalesforceServiceTest : BaseTriageEndpointTest() {

    @Autowired
    lateinit var imageService: TriageImageService

    @BeforeAll
    override fun readSchema() {
        super.readSchema()

        val q4 = createBooleanQuestion("q4", 4, ActionType.GO_TO_QUESTION, 5, null)
        val q3 = createBooleanQuestion("q3", 3, ActionType.FINISH, 3, null)
        val q2 = createBooleanQuestion("q2", 2, ActionType.GO_TO_QUESTION, 4, q3)
        val q1 = createBooleanQuestion("q1", 1, ActionType.GO_TO_QUESTION, 5, q2)
        // add a test questionnaire with questions of different urgencies
        questionnaireModel.addQuestionnaire(
            QuestionnaireDefinition(TEST_QUESTIONNAIRE, "Test", listOf(q1, q2, q3, q4))
        )
    }

    @BeforeEach
    fun beforeEach() {
        // reset invocation counter
        reset(salesforceService)
    }

    @Test
    fun `given triage for myself and triage ended with GO_TO_CHAT, expect valid object`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(keelk1Question, triageStatus.id, 5)
        val keelk2Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK2")
        saveAnswer(keelk2Question, triageStatus.id, 1)

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_CHAT)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus))

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals("Keelklachten", capturedDto.subject)
        assertEquals("Keelklachten", capturedDto.category)
        assertEquals("", capturedDto.question)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(false, capturedDto.triageStopped)
        assertEquals(UNKNOWN_URGENCY, capturedDto.urgency)
        assertEquals(4, capturedDto.triageAnswers.size)
        assertEquals(2, capturedDto.triageAdditionalAnswers?.size)
        assertNull(capturedDto.triagePerson)
    }

    @Test
    fun `given triage for others and triage ended with GO_TO_CHAT, expect valid object`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 2)
        saveAnswer(
            questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, RELATION_QUESTION_UNIQUE_ID),
            triageStatus.id,
            4
        )
        saveAnswer(
            questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, FIRSTNAME_QUESTION_UNIQUE_ID),
            triageStatus.id,
            "Rocky"
        )
        saveAnswer(
            questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, LASTNAME_QUESTION_UNIQUE_ID),
            triageStatus.id,
            "Balboa"
        )
        saveAnswer(questionnaireModel.genderProfileQuestion, triageStatus.id, 2)
        saveAnswer(
            questionnaireModel.birthdayProfileQuestion.uniqueQuestionId,
            triageStatus.id,
            DateAnswer(BIRTHDAY_QUESTION_UNIQUE_ID, LocalDate.of(1945, 7, 6))
        )
        saveAnswer(
            questionnaireModel.getQuestionByUniqueId(PROFILE_QUESTIONNAIRE_ID, BSN_QUESTION_UNIQUE_ID),
            triageStatus.id,
            "123456789"
        )
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(keelk1Question, triageStatus.id, 5)
        val keelk2Question = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK2")
        saveAnswer(keelk2Question, triageStatus.id, 1)

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_CHAT)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus))

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals("Keelklachten", capturedDto.subject)
        assertEquals("Keelklachten", capturedDto.category)
        assertEquals("", capturedDto.question)
        assertEquals(false, capturedDto.selfTriage)
        assertEquals(false, capturedDto.triageStopped)
        assertEquals(UNKNOWN_URGENCY, capturedDto.urgency)
        assertEquals(10, capturedDto.triageAnswers.size)
        assertEquals(2, capturedDto.triageAdditionalAnswers?.size)
        assertNotNull(capturedDto.triagePerson)
        assertEquals("Anders", capturedDto.triagePerson?.relation)
        assertEquals("Rocky", capturedDto.triagePerson?.firstname)
        assertEquals("Balboa", capturedDto.triagePerson?.lastname)
        assertEquals("male", capturedDto.triagePerson?.gender)
        assertEquals("1945-07-06", capturedDto.triagePerson?.birthdate)
        assertEquals("123456789", capturedDto.triagePerson?.bsn)
    }

    @Test
    fun `given triage for myself and triage ended with FINISHED, expect valid object`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(keelk1Question, triageStatus.id, 5)
        var nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK2")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK5")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6")
        saveAnswer(nextQuestion, triageStatus.id, 1)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6A")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK10A")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK10B")
        saveAnswer(nextQuestion, triageStatus.id, 5)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG14")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG15")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG21")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG16")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG1A")
        saveAnswer(nextQuestion, triageStatus.id, 13)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG3B")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG5")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG9")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", ADDITIONAL_INFO_QUESTION_UNIQUE_ID)
        saveAnswer(nextQuestion, triageStatus.id, "Can I ask a custom question now?")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG27")
        saveAnswer(nextQuestion, triageStatus.id, "Can I finish?")

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus))

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals("Keelklachten", capturedDto.subject)
        assertEquals("Keelklachten", capturedDto.category)
        assertEquals("Can I ask a custom question now?", capturedDto.question)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(false, capturedDto.triageStopped)
        assertEquals("U2", capturedDto.urgency)
        assertEquals(21, capturedDto.triageAnswers.size)
        assertEquals(19, capturedDto.triageAdditionalAnswers?.size)
    }

    @Test
    fun `given additional question is skipped, expect valid object`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(keelk1Question, triageStatus.id, 5)
        var nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG9")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", ADDITIONAL_INFO_QUESTION_UNIQUE_ID)
        saveAnswer(nextQuestion.uniqueQuestionId, triageStatus.id, SkipAnswer(nextQuestion.uniqueQuestionId))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG27")
        saveAnswer(nextQuestion, triageStatus.id, "Can I finish?")

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus))

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals("Keelklachten", capturedDto.subject)
        assertEquals("Keelklachten", capturedDto.category)
        assertEquals("overgeslagen", capturedDto.question)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(false, capturedDto.triageStopped)
        assertEquals("", capturedDto.urgency)
        assertEquals(6, capturedDto.triageAnswers.size)
        assertEquals(3, capturedDto.triageAdditionalAnswers?.size)
    }

    @Test
    fun `given additional question is 'uncertain', expect valid object`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(keelk1Question, triageStatus.id, 5)
        var nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK2")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK4")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK5")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6")
        saveAnswer(nextQuestion, triageStatus.id, 1)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK6A")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK10A")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK10B")
        saveAnswer(nextQuestion, triageStatus.id, 5)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG14")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG15")
        saveAnswer(nextQuestion, triageStatus.id, "description")
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG21")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG16")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG1A")
        saveAnswer(nextQuestion, triageStatus.id, 13)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG3B")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG5")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG9")
        saveAnswer(nextQuestion, triageStatus.id, 2)
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", ADDITIONAL_INFO_QUESTION_UNIQUE_ID)
        saveAnswer(nextQuestion.uniqueQuestionId, triageStatus.id, UncertainAnswer(nextQuestion.uniqueQuestionId))
        nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "ALG27")
        saveAnswer(nextQuestion, triageStatus.id, "Can I finish?")

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus))

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals("Keelklachten", capturedDto.subject)
        assertEquals("Keelklachten", capturedDto.category)
        assertEquals("weet ik niet", capturedDto.question)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(false, capturedDto.triageStopped)
        assertEquals("U2", capturedDto.urgency)
        assertEquals(21, capturedDto.triageAnswers.size)
        assertEquals(18, capturedDto.triageAdditionalAnswers?.size)
    }

    @Test
    fun `given triage for myself and triage ended with FINISHED, expect valid object with urgency U3`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaAnswer(triageStatus.id, TEST_QUESTIONNAIRE)
        var nextQuestion = questionnaireModel.findFirstQuestion(TEST_QUESTIONNAIRE)
        saveAnswer(nextQuestion, triageStatus.id, 1)
        nextQuestion = questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q2")
        saveAnswer(nextQuestion, triageStatus.id, 1)
        nextQuestion = questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q3")
        saveAnswer(nextQuestion, triageStatus.id, 1)
        nextQuestion = questionnaireModel.getQuestionByUniqueId(TEST_QUESTIONNAIRE, "q4")
        saveAnswer(nextQuestion, triageStatus.id, 1)

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus))

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals("Test", capturedDto.subject)
        assertEquals("Test", capturedDto.category)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(false, capturedDto.triageStopped)
        assertEquals("U3", capturedDto.urgency)
        assertEquals(6, capturedDto.triageAnswers.size)
        assertEquals(0, capturedDto.triageAdditionalAnswers?.size)
        val q1Answer = capturedDto.triageAnswers
            .find { it.shortQuestion == "q1" }
            ?: throw IllegalStateException("No answer with questionId=q1")
        assertEquals("Boolean question q1", q1Answer.question)
        assertEquals("Ja", q1Answer.chosenAnswer)
        assertEquals("Ja|Nee", q1Answer.possibleAnswers)
    }

    @Test
    fun `given triage for myself and triage ended with FINISHED_BY_USER, expect valid object`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(keelk1Question, triageStatus.id, 5)
        val nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK2")
        saveAnswer(nextQuestion, triageStatus.id, 2)

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_USER_WITH_CHAT)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus))

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals("Keelklachten", capturedDto.subject)
        assertEquals("Keelklachten", capturedDto.category)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(true, capturedDto.triageStopped)
    }

    @Test
    fun `given triage for myself and triage ended with FINISHED_BY_USER_OTHER, expect valid object`() {
        testWhenStoppedOnMedarea(TriageProgress.FINISHED_BY_USER_OTHER, CATEGORY_OTHER_COMPLAINT, true)
    }

    @Test
    fun `given triage for myself and triage ended with FINISHED_BY_USER_WITH_CHAT, expect valid object`() {
        testWhenStoppedOnMedarea(TriageProgress.FINISHED_BY_USER_WITH_CHAT, "", false)
    }

    @Test
    fun `given triage for myself and triage ended with FINISHED_BY_CHAT after specifying that the medical area is not in the list, subject is 'Mijn klacht staat er niet tussen'`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveAnswer(questionnaireModel.getQuestionByUniqueId("OVERIG", "OVERIABCDE1A"), triageStatus.id, 1)

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_CHAT)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus), true)

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals(CATEGORY_OTHER_COMPLAINT, capturedDto.subject)
        assertEquals(CATEGORY_OTHER_COMPLAINT, capturedDto.category)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(false, capturedDto.triageStopped)
    }

    private fun testWhenStoppedOnMedarea(triageProgress: TriageProgress, category: String, questionnaireIsOther: Boolean) {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, triageProgress)

        triageSalesforceService.sendTriageAnswers(triageService.fetchTriageData(triageStatus), questionnaireIsOther)

        val argumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(argumentCaptor.capture())

        val capturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, capturedDto.chatId)
        assertEquals(user.publicId, capturedDto.customerId)
        assertEquals(label.code, capturedDto.labelCode)
        assertEquals(category, capturedDto.subject)
        assertEquals(category, capturedDto.category)
        assertEquals(true, capturedDto.selfTriage)
        assertEquals(true, capturedDto.triageStopped)
    }

    @Test
    fun `given triage with image uploaded and triage ended with FINISHED_BY_USER, expect valid images object`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        var triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        saveMedicalAreaKEELKAnswer(triageStatus.id)
        val keelk1Question = questionnaireModel.findFirstQuestion("KEELK")
        saveAnswer(keelk1Question, triageStatus.id, 5)
        val nextQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK2")
        saveAnswer(nextQuestion, triageStatus.id, 2)

        triageStatus = triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED_BY_CHAT)

        val stream = ByteArrayInputStream("test".toByteArray())
        val contentType = "image/png"
        val imageId = imageService.upload(label.code, triageStatus.id, contentType, stream)

        val descriptiveWithImageQuestion = questionnaireModel.getQuestionByUniqueId("KEELK", "KEELK9A")
        triageAnswerDao.saveNew(
            triageStatus.id,
            descriptiveWithImageQuestion.uniqueQuestionId,
            DescriptiveWithImages(descriptiveWithImageQuestion.uniqueQuestionId, "abc", listOf(imageId)).toJson(),
            ImagesAnswer(descriptiveWithImageQuestion.uniqueQuestionId, "abc", listOf(imageId)).toJson()
        )

        val triageData = triageService.fetchTriageData(triageStatus)
        triageSalesforceService.sendTriageAnswers(triageData)

        val answerArgumentCaptor = argumentCaptor<SalesforceTriageAnswersRequestDto>()
        verify(salesforceService).sendTriageAnswers(answerArgumentCaptor.capture())

        val argumentCaptor = argumentCaptor<SalesforceTriageAttachmentData>()
        verify(salesforceService).addImagesToTriageAsync(argumentCaptor.capture(), any())

        val answerCapturedDto = answerArgumentCaptor.firstValue
        val attachmentCapturedDto = argumentCaptor.firstValue

        assertEquals(conversation.publicId, attachmentCapturedDto.conversationId)
        assertEquals(user.publicId, attachmentCapturedDto.userId)
        assertEquals(1, attachmentCapturedDto.attachments.size)
        assertEquals(imageId, attachmentCapturedDto.attachments[0].imageIds[0])
        val answerWithImage = answerCapturedDto.triageAnswers
            .find { it.shortQuestion == descriptiveWithImageQuestion.shortQuestionVpk }
            ?.chosenAnswer
            ?: throw IllegalStateException("No answer with questionId=${descriptiveWithImageQuestion.uniqueQuestionId}")
        assertEquals("abc >> zie 1 bijlage(s)", answerWithImage)
    }
}
