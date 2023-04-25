package com.innovattic.medicinfo.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.test.RestAssuredConfig
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.ConfigureMirroDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestIntroductionDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestMirroDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestOutcomeDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestProblemAreaDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestProblemAreasDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestQuestionDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestValueDto
import com.innovattic.medicinfo.logic.dto.SelfTestQuestionType
import com.innovattic.medicinfo.logic.dto.SelfTestSeverity
import com.innovattic.medicinfo.test.BaseIntegrationTest
import com.innovattic.medicinfo.web.security.AuthenticationService
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.doAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import java.util.*

abstract class BaseEndpointTest : BaseIntegrationTest() {
    val adminAccessToken by lazy { accessToken(userService.adminUser) }

    fun configureSelfTestDto(
        problemAreas: List<ConfigureSelfTestProblemAreaDto> = listOf(
            configureSelfTestProblemAreaDto(
                "code-problem-area-configure-1",
                listOf(
                    booleanQuestion("code-problem-area-question-1", 1, 0),
                    singleChoiceQuestion(
                        "code-problem-area-question-2",
                        "code-single-choice-choice-1", "code-single-choice-choice-2", "code-single-choice-choice-3"
                    ),
                    sliderQuestion("code-problem-area-question-3", 1, 4)
                ),
                defaultOutcomes(),
            ),
            configureSelfTestProblemAreaDto(
                "code-problem-area-configure-2",
                listOf(booleanQuestion("code-problem-area-2-question-1", 5, 0))
            )
        ),
        generalQuestions: List<ConfigureSelfTestQuestionDto> = listOf(
            singleChoiceQuestion(
                "general-question-1",
                "code-single-choice-choice-1", "code-single-choice-choice-2", "code-single-choice-choice-3"
            )
        ),
        personalAssistance: ConfigureSelfTestQuestionDto = booleanQuestion(
            "code-personal-assistance",
            scoreForTrue = 1,
            scoreForFalse = 1
        ),
    ) = ConfigureSelfTestDto(
        ConfigureSelfTestIntroductionDto("introduction"),
        ConfigureSelfTestProblemAreasDto("problem-areas-title", "problem-areas-description", problemAreas, 3),
        generalQuestions,
        personalAssistance,
        "general-advice-for-barely-text"
    )

    fun configureSelfTestProblemAreaDto(
        code: String,
        questions: List<ConfigureSelfTestQuestionDto>,
        outcomes: List<ConfigureSelfTestOutcomeDto> = simpleOutcomes(),
        name: String = "Name of $code",
        mirro: ConfigureMirroDto = ConfigureMirroDto(
            "mirro-title",
            "mirro-description",
            "mirro-long-description",
            "https://test.mirro/module/42"
        )
    ) = ConfigureSelfTestProblemAreaDto(code, name, questions, outcomes, mirro)

    fun booleanQuestion(
        code: String,
        scoreForTrue: Int = 1,
        scoreForFalse: Int = 0,
        title: String = "title-boolean",
        description: String = "description-boolean",
    ) = question(
        code, SelfTestQuestionType.BOOLEAN,
        ConfigureSelfTestValueDto(null, true, scoreForTrue), ConfigureSelfTestValueDto(null, false, scoreForFalse),
        title = title, description = description
    )

    fun sliderQuestion(
        code: String,
        min: Int = 1,
        max: Int = 10,
        title: String = "title-slider",
        description: String = "description-slider",
    ) = ConfigureSelfTestQuestionDto(code, SelfTestQuestionType.SLIDER, title, description,
        (min..max).map { ConfigureSelfTestValueDto(null, it, it) })

    fun singleChoiceQuestion(
        code: String,
        vararg valueCodes: String,
        title: String = "title-choice",
        description: String = "description-choice",
    ) = singleChoiceQuestion(
        code,
        valueCodes.mapIndexed { index, it -> choiceValue(it, index + 1, "Value for $it") },
        title,
        description
    )

    fun singleChoiceQuestion(
        code: String,
        values: List<ConfigureSelfTestValueDto>,
        title: String = "title-single-choice",
        description: String = "description-single-choice",
    ) = ConfigureSelfTestQuestionDto(code, SelfTestQuestionType.SINGLE_CHOICE, title, description, values)

    fun question(
        code: String,
        type: SelfTestQuestionType,
        vararg values: ConfigureSelfTestValueDto,
        title: String = "title",
        description: String? = "description",
    ) = ConfigureSelfTestQuestionDto(code, type, title, description, values.toList())

    fun booleanValue(value: Boolean, score: Int = 1) = ConfigureSelfTestValueDto(null, value, score)
    fun sliderValue(value: Int, score: Int = value) = ConfigureSelfTestValueDto(null, value, score)
    fun choiceValue(code: String, score: Int = 1, value: String = "Value for $code") =
        ConfigureSelfTestValueDto(code, value, score)

    fun simpleOutcomes() = listOf(
        outcome(SelfTestSeverity.LIGHT, "light-advice", "light-chat"),
        outcome(SelfTestSeverity.MEDIUM, "mild-advice", "mild-chat"),
        outcome(SelfTestSeverity.SERIOUS, "serious-advice", "serious-chat")
    )

    fun defaultOutcomes() = listOf(
        outcome(
            SelfTestSeverity.LIGHT, "light-advice", "light-chat",
            ConfigureSelfTestMirroDto(false, "not-needed")
        ),
        outcome(
            SelfTestSeverity.MEDIUM, "mild-advice", "mild-chat",
            ConfigureSelfTestMirroDto(false)
        ),
        outcome(
            SelfTestSeverity.SERIOUS, "serious-advice-with-personal", "serious-chat-with-personal",
            ConfigureSelfTestMirroDto(false, "any-reason"),
            personalAssistance = true,
        ),
        outcome(
            SelfTestSeverity.SERIOUS, "serious-advice-with-mirro", "serious-chat-with-mirro",
            ConfigureSelfTestMirroDto(true),
            personalAssistance = false
        )
    )

    fun outcome(
        severity: SelfTestSeverity,
        adviceText: String = "advice-text",
        chatText: String = "chat-text",
        mirro: ConfigureSelfTestMirroDto? = ConfigureSelfTestMirroDto(false),
        personalAssistance: Boolean? = null,
    ) = ConfigureSelfTestOutcomeDto(severity, adviceText, chatText, mirro, personalAssistance)

    val configureSelfTestDto = configureSelfTestDto()

    @BeforeAll
    fun setUpRestAssured(@LocalServerPort port: Int, @Autowired mapper: ObjectMapper) {
        RestAssuredConfig.setupRestAssured(port, mapper, "api/")
    }

    @BeforeEach
    fun mockLocalazyService() {
        doAnswer{it.arguments.last()}.`when`(localazyService).getTranslation(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString()
        )
    }

    fun ValidatableResponse.assertUser(prefix: String, actual: User) {
        body(prefix + "id", equalTo(actual.publicId.toString()))
        body(prefix + "created", notNullValue())
        body(prefix + "role", equalTo(actual.role.value))
        body(prefix + "displayName", equalTo(actual.name))
        body(prefix + "email", equalTo(actual.email))
        body(prefix + "gender", equalTo(actual.gender?.value))
        body(prefix + "age", equalTo(actual.age))
        body(prefix + "isInsured", equalTo(actual.isInsured))
        body(prefix + "labelId", if (actual.labelId == null) nullValue() else notNullValue())
    }

    fun accessToken(user: User) =
        authenticationService.createAccessToken(clock.instant(), user, AuthenticationService.APP_VALIDITY)

    fun clearAllUsersExceptMainAdmin() = userDao.clear(userService.adminUser.id)

    fun findMessageAttachment(conversationPojo: Conversation, messagePublicId: UUID) =
        messageDao.get(conversationPojo.id, null, null)
            .filter { it.publicId == messagePublicId }
            .map { messageAttachmentDao.getById(it.attachmentId) }
            .singleOrNull() ?: throw IllegalStateException("Should not be null! This has to have one element")
}
