package com.innovattic.medicinfo.logic

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.error.failResponseIf
import com.innovattic.medicinfo.database.dao.AppSelfTestDao
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.AppSelfTestDto
import com.innovattic.medicinfo.logic.dto.AppSelfTestIntroductionDto
import com.innovattic.medicinfo.logic.dto.AppSelfTestProblemAreaDto
import com.innovattic.medicinfo.logic.dto.AppSelfTestProblemAreasDto
import com.innovattic.medicinfo.logic.dto.AppSelfTestQuestionDto
import com.innovattic.medicinfo.logic.dto.AppSelfTestAnswerDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestProblemAreaDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestQuestionDto
import com.innovattic.medicinfo.logic.dto.MirroDto
import com.innovattic.medicinfo.logic.dto.SelfTestAdviceDto
import com.innovattic.medicinfo.logic.dto.SelfTestAnswerDto
import com.innovattic.medicinfo.logic.dto.SelfTestAnswersDto
import com.innovattic.medicinfo.logic.dto.SelfTestProblemAreaDto
import com.innovattic.medicinfo.logic.dto.SelfTestQuestionType
import com.innovattic.medicinfo.logic.dto.SelfTestResultDto
import com.innovattic.medicinfo.logic.dto.SelfTestResultsDto
import com.innovattic.medicinfo.logic.dto.SelfTestSeverity
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*

@Component
class SelfTestService(
    private val dao: AppSelfTestDao,
    private val labelDao: LabelDao,
    private val objectMapper: ObjectMapper,
    private val userSelfTestResultService: UserSelfTestResultService,
    private val salesforceService: SalesforceService,
    private val conversationDao: ConversationDao,
) {
    fun createOrUpdate(labelId: UUID, configureSelfTestDto: ConfigureSelfTestDto) {
        val label = labelDao.getByPublicId(labelId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label not found" }
        val appSelfTest = dao.getByLabelId(label.id)
        val data = objectMapper.writeValueAsString(configureSelfTestDto)

        validateSelfTest(configureSelfTestDto)

        if (appSelfTest != null) {
            dao.update(appSelfTest.id, data)
        } else {
            dao.create(label.id, data)
        }
    }

    fun get(user: User): AppSelfTestDto {
        return get(user.labelId)
    }

    fun get(labelId: Int): AppSelfTestDto {
        return mapSelfTestDto(getConfiguration(labelId))
    }

    private fun validateSelfTest(dto: ConfigureSelfTestDto) {
        failResponseIf(dto.personalAssistance!!.type != SelfTestQuestionType.BOOLEAN) {
            "Personal assistance question must be of type boolean"
        }

        val problemAreas = dto.problemAreaQuestion!!.problemAreas!!
        problemAreas.forEach(::validateProblemArea)

        val questions = problemAreas.flatMap { it.questions!! } + dto.generalQuestions!! + dto.personalAssistance
        validateUnique(questions.map { it.code!! }, "Duplicate question codes")
        questions.forEach(::validateQuestion)
    }

    private fun validateProblemArea(dto: ConfigureSelfTestProblemAreaDto) {
        dto.outcomes!!.groupBy { it.severity }
            .forEach { (severity, dtos) ->
                val message = "Invalid outcomes with severity $severity in problem area ${dto.code}: " +
                    "Must have either a single entry with personalAssistance = null, or two entries with true and false."
                failResponseIf(dtos.size > 2) { message }
                failResponseIf(dtos.size == 1 && dtos.single().personalAssistance != null) { message }
                failResponseIf(
                    dtos.size == 2 &&
                        dtos.count { it.personalAssistance == true } != 1 && dtos.count { it.personalAssistance == false } != 1
                ) { message }
            }
    }

    private fun validateQuestion(dto: ConfigureSelfTestQuestionDto) {
        when (dto.type!!) {
            SelfTestQuestionType.BOOLEAN -> validateBooleanQuestion(dto)
            SelfTestQuestionType.SLIDER -> validateSliderQuestion(dto)
            SelfTestQuestionType.SINGLE_CHOICE -> validateSingleChoiceQuestion(dto)
        }
    }

    private fun validateBooleanQuestion(dto: ConfigureSelfTestQuestionDto) {
        failResponseIf(dto.answers!!.size != 2) { "Boolean question ${dto.code} must have exactly two answers" }

        val values = dto.answers.map { it.value!! }
        failResponseIf(values.count { it == true } != 1) { "Boolean question ${dto.code} must have exactly one answer with value true" }
        failResponseIf(values.count { it == false } != 1) { "Boolean question ${dto.code} must have exactly one answer with value false" }
    }

    private fun validateSliderQuestion(dto: ConfigureSelfTestQuestionDto) {
        val invalids = dto.answers!!.filter { it.value !is Int }
        failResponseIf(invalids.isNotEmpty()) { "Invalid answer values in slider question ${dto.code}: $invalids" }

        val values = dto.answers.map { it.value as Int }
        val min = values.minOrNull()!!
        val max = values.maxOrNull()!!
        (min..max).forEach {
            failResponseIf(values.count(it::equals) != 1) { "Slider question ${dto.code} must have exactly one answer with value $it" }
        }
    }

    private fun validateSingleChoiceQuestion(dto: ConfigureSelfTestQuestionDto) {
        val invalids = dto.answers!!.filter { it.value !is String }
        failResponseIf(invalids.isNotEmpty()) { "Invalid answer values in single choice question ${dto.code}: $invalids" }

        validateUnique(dto.answers.map { it.code!! }, "Duplicate answer codes in single choice question ${dto.code}")
        validateUnique(
            dto.answers.map { it.value as String },
            "Duplicate answer values in single choice question ${dto.code}"
        )
    }

    private fun <T> validateUnique(elements: List<T>, message: String) {
        val duplicates = elements.distinct().filter { elements.count(it!!::equals) > 1 }
        failResponseIf(duplicates.isNotEmpty()) { "$message: $duplicates" }
    }

    private fun getConfiguration(labelId: Int): ConfigureSelfTestDto {
        val appSelfTest = dao.getByLabelId(labelId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "No SelfTest configured for your label" }
        return objectMapper.readValue(appSelfTest.data, ConfigureSelfTestDto::class.java)
    }

    private fun mapSelfTestDto(dto: ConfigureSelfTestDto) =
        AppSelfTestDto(
            AppSelfTestIntroductionDto(dto.introduction!!.text!!),
            AppSelfTestProblemAreasDto(
                dto.problemAreaQuestion!!.title!!,
                dto.problemAreaQuestion.description!!,
                dto.problemAreaQuestion.problemAreas!!.map {
                    AppSelfTestProblemAreaDto(
                        it.code!!,
                        it.name!!,
                        it.title,
                        it.questions!!.map(::mapQuestionDto),
                        MirroDto(
                            it.mirro?.title, it.mirro?.shortDescription, it.mirro?.longDescription, it.mirro?.link,
                            it.mirro?.link?.let { link -> extractBlogIdFromLink(link) }
                        )
                    )
                },
                dto.problemAreaQuestion.maximumAreas!!
            ),
            dto.generalQuestions?.map { mapQuestionDto(it) }.orEmpty(),
            mapQuestionDto(dto.personalAssistance!!),
            dto.generalAdviceForBarely,
            dto.generalAdviceForBarely
        )

    private fun extractBlogIdFromLink(link: String): Int {
        try {
            return link.substringAfterLast("/").toInt()
        } catch (e: NumberFormatException) {
            throw createResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR) { "Malformed Mirro link in selftest configuration" }
        }
    }

    private fun mapQuestionDto(dto: ConfigureSelfTestQuestionDto): AppSelfTestQuestionDto {
        return when (dto.type!!) {
            SelfTestQuestionType.BOOLEAN -> {
                AppSelfTestQuestionDto(dto.code!!, dto.type, dto.title!!, dto.description)
            }
            SelfTestQuestionType.SINGLE_CHOICE -> {
                val values = dto.answers!!.map { AppSelfTestAnswerDto(it.code!!, it.value as String) }
                AppSelfTestQuestionDto(dto.code!!, dto.type, dto.title!!, dto.description, values)
            }
            SelfTestQuestionType.SLIDER -> {
                val min = dto.answers!!.minOf { it.value as Int }
                val max = dto.answers.maxOf { it.value as Int }
                AppSelfTestQuestionDto(dto.code!!, dto.type, dto.title!!, dto.description, null, min, max)
            }
        }
    }

    fun submitSelfTest(user: User, dto: SelfTestAnswersDto): SelfTestResultsDto {
        val results = getSelfTestResults(getConfiguration(user.labelId), dto)
        userSelfTestResultService.createOrUpdate(user.id, user.labelId, dto)
        return results
    }

    private fun getSelfTestResults(
        configuration: ConfigureSelfTestDto,
        dto: SelfTestAnswersDto
    ): SelfTestResultsDto {
        val selectedProblemAreas =
            configuration.problemAreaQuestion!!.problemAreas!!.filter { (it.code!! in dto.selectedProblemAreaCodes!!) }

        val generalQuestionsByCode = configuration.generalQuestions!!.associateBy { it.code!! }
        val generalQuestionsScore = dto.answers!!.sumOf {
            val question = generalQuestionsByCode[it.questionCode] ?: return@sumOf 0
            calculatePoints(it, question)
        }

        val results = selectedProblemAreas.map { problemArea ->
            val questionsByCode = problemArea.questions!!.associateBy { it.code!! }
            val problemAreaScore = dto.answers.sumOf {
                val question = questionsByCode[it.questionCode] ?: return@sumOf 0
                calculatePoints(it, question)
            }

            val severity = gradeSeverity(problemArea.questions.size, problemAreaScore, generalQuestionsScore)
            SelfTestResultDto(problemArea.code!!, severity, problemAreaScore)
        }.sortedByDescending { it.score }

        return SelfTestResultsDto(results, true)
    }

    fun selectProblemArea(user: User, dto: SelfTestProblemAreaDto): SelfTestAdviceDto {
        val configuration = getConfiguration(user.labelId)
        val results = userSelfTestResultService.get(user.id)
            ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Self test results must be submitted first" }

        val problemArea = configuration.problemAreaQuestion!!.problemAreas!!.find { it.code == dto.problemAreaCode }
            ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Invalid problem area code ${dto.problemAreaCode}" }

        val selfTestAdvice = generateSelfTestAdvice(problemArea, dto)

        val questionAndAnswers = formatQuestionsAndAnswers(results, configuration)
        val result = formatResult(selfTestAdvice)
        val maybeLatestConversation = conversationDao.getLatest(user.id)

        salesforceService.submitSelfTestAsync(
            label = labelDao.getById(user.labelId)!!,
            user = user,
            complaintArea = dto.problemAreaCode!!,
            degreeOfComplaints = dto.severity.salesforceValue,
            personalAssistance = if (dto.personalAssistance!!) "Ja" else "Nee",
            questionsAndAnswers = questionAndAnswers,
            result = result,
            conversationId = maybeLatestConversation?.publicId
        )

        return selfTestAdvice
    }

    fun selectNoProblemArea(user: User): SelfTestAdviceDto {
        val configuration = getConfiguration(user.labelId)
        val results = userSelfTestResultService.get(user.id)
            ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Self test results must be submitted first" }

        val questionAndAnswers = formatQuestionsAndAnswers(results, configuration)
        val maybeLatestConversation = conversationDao.getLatest(user.id)

        salesforceService.submitSelfTestAsync(
            label = labelDao.getById(user.labelId)!!,
            user = user,
            complaintArea = null,
            degreeOfComplaints = SelfTestSeverity.BARELY.salesforceValue,
            personalAssistance = "n.v.t.",
            questionsAndAnswers = questionAndAnswers,
            result = configuration.generalAdviceForBarely,
            conversationId = maybeLatestConversation?.publicId
        )

        return SelfTestAdviceDto(configuration.generalAdviceForBarely, null, null)
    }

    private fun formatResult(
        selfTestAdviceDto: SelfTestAdviceDto
    ): String {
        return listOfNotNull(
            selfTestAdviceDto.adviceText,
            selfTestAdviceDto.chatText,
            if (selfTestAdviceDto.mirro?.use == true) {
                "Er is een mirro module aangeboden."
            } else {
                "Er is geen mirro module aangeboden."
            }
        ).joinToString("<br/>")
    }

    private fun formatQuestionsAndAnswers(
        answers: SelfTestAnswersDto,
        appSelfTest: ConfigureSelfTestDto
    ): String {
        val allQuestions =
            appSelfTest.problemAreaQuestion!!.problemAreas!!.flatMap { it.questions!! } + appSelfTest.generalQuestions!!

        return answers.answers!!.map { answer ->
            allQuestions.find { it.code == answer.questionCode }?.let { question ->
                (question.description ?: question.title) + ": " + formatAnswer(answer, question) + ";"
            }
        }.joinToString("<br/>")
    }

    private fun formatAnswer(answer: SelfTestAnswerDto, question: ConfigureSelfTestQuestionDto): String {
        return if (answer.booleanAnswer != null) {
            if (answer.booleanAnswer == true) "Ja" else "Nee"
        } else if (answer.sliderAnswer != null) {
            "${answer.sliderAnswer}"
        } else if (answer.singleChoiceAnswerCode != null) {
            question.answers!!.find { it.code == answer.singleChoiceAnswerCode }?.value?.toString() ?: ""
        } else ""
    }

    private fun generateSelfTestAdvice(
        problemArea: ConfigureSelfTestProblemAreaDto,
        dto: SelfTestProblemAreaDto
    ): SelfTestAdviceDto {
        return problemArea.outcomes!!
            .filter {
                it.personalAssistance == null || it.personalAssistance == dto.personalAssistance
            }
            .find { it.severity == dto.severity }
            ?.let { SelfTestAdviceDto(it.adviceText, it.chatText, it.mirro) }
            ?: SelfTestAdviceDto()
    }

    private fun calculatePoints(answer: SelfTestAnswerDto, question: ConfigureSelfTestQuestionDto): Int {
        return when (question.type!!) {
            SelfTestQuestionType.BOOLEAN -> {
                val value =
                    answer.booleanAnswer
                        ?: throw createResponseStatusException { "Missing answer for question ${question.code}" }
                question.answers!!.single { it.value == value }.score!!
            }
            SelfTestQuestionType.SLIDER -> {
                val value = answer.sliderAnswer
                    ?: throw createResponseStatusException { "Missing answer for question ${question.code}" }
                question.answers!!.singleOrNull { it.value == value }?.score
                    ?: throw createResponseStatusException { "Answer $value out of range for slider question ${question.code}" }
            }
            SelfTestQuestionType.SINGLE_CHOICE -> {
                val value = answer.singleChoiceAnswerCode
                    ?: throw createResponseStatusException { "Missing answer for question ${question.code}" }
                question.answers!!.singleOrNull { it.code == value }?.score
                    ?: throw createResponseStatusException { "Invalid answer code $value for single choice question ${question.code}" }
            }
        }
    }

    private fun gradeSeverity(
        questionsAmount: Int,
        problemAreaPoints: Int,
        generalQuestionPoints: Int
    ): SelfTestSeverity {
        val averageScore = problemAreaPoints.toDouble() / questionsAmount

        return if (averageScore >= 2.5 || generalQuestionPoints >= 3) {
            SelfTestSeverity.SERIOUS
        } else if (averageScore > 2 || generalQuestionPoints == 2) {
            SelfTestSeverity.MEDIUM
        } else if (averageScore > 1) {
            SelfTestSeverity.LIGHT
        } else {
            SelfTestSeverity.BARELY
        }
    }
}
