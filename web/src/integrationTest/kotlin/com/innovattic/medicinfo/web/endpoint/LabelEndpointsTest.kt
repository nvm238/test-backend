package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertEmptyResponse
import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dao.ConfigurePushNotificationMessageDto
import com.innovattic.medicinfo.database.dto.LabelDto
import com.innovattic.medicinfo.dbschema.tables.pojos.Label
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestDto
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestIntroductionDto
import com.innovattic.medicinfo.logic.dto.SelfTestQuestionType
import com.innovattic.medicinfo.logic.dto.SelfTestSeverity
import com.innovattic.medicinfo.logic.dto.UpdateLabelDto
import com.innovattic.medicinfo.web.BaseEndpointTest
import com.innovattic.medicinfo.web.dto.ApiKeyDto
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class LabelEndpointsTest : BaseEndpointTest() {

    @BeforeEach
    fun beforeEach() {
        clock.reset()
    }

    @AfterEach
    fun afterEach() {
        clock.reset()
    }

    @Test
    fun getByCode_works_withoutAuth() {
        val label = createLabel(withPushNotifications = true)
        When {
            get("v1/label/code/${label.code}")
        } Then {
            assertObjectResponse()
            body("id", equalTo(label.publicId.toString()))
            body("created", nullValue())
            body("code", equalTo(label.code))
            body("name", equalTo(label.name))
            body("hasPushNotifications", equalTo(true))
            body("fcmApiKey", nullValue())
        }
    }

    @Test
    fun getMyLabel_works_forCustomer() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/label/my")
        } Then {
            assertObjectResponse()
            body("id", equalTo(label.publicId.toString()))
            body("created", notNullValue())
            body("code", equalTo(label.code))
            body("name", equalTo(label.name))
            body("hasPushNotifications", equalTo(false))
            body("fcmApiKey", nullValue())
        }
    }

    @Test
    fun getMyLabel_fails_forEmployee() {
        val user = createEmployee("e1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/label/my")
        } Then {
            assertErrorResponse(null, HttpStatus.FORBIDDEN)
        }
    }

    @Test
    fun createLabel_works() {
        val code = "test-label-" + UUID.randomUUID()
        Given {
            auth().oauth2(adminAccessToken)
            body(LabelDto(code = code, name = "Test label", fcmApiKey = "test-api-key"))
        } When {
            post("v1/label")
        } Then {
            assertObjectResponse()
            body("id", notNullValue())
            body("created", notNullValue())
            body("code", equalTo(code))
            body("name", equalTo("Test label"))
            body("hasPushNotifications", equalTo(true))
            body("fcmApiKey", nullValue())
        }
    }

    @Test
    fun createLabel_fails_whenDuplicateCode() {
        val label = createLabel()
        Given {
            auth().oauth2(adminAccessToken)
            body(LabelDto(code = label.code, name = "Test label"))
        } When {
            post("v1/label")
        } Then {
            assertErrorResponse(ErrorCodes.DUPLICATE_CODE, errorCodeField = "errorCode")
        }
    }

    @Test
    fun configurePushNotifications_works() {
        val label = createLabel()
        Given {
            auth().oauth2(adminAccessToken)
            body(ApiKeyDto("test-key"))
        } When {
            post("v1/label/${label.publicId}/configure-push-notifications")
        } Then {
            assertEmptyResponse()
        }
        assertNotNull(labelDao.getById(label.id)!!.fcmApiKey)
    }

    @Test
    fun configurePushNotificationText_works() {
        val label = createLabel()
        val dto = ConfigurePushNotificationMessageDto("Er staat een nieuw bericht voor je klaar")
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            post("v1/label/${label.publicId}/configure-push-notification-message")
        } Then {
            assertEmptyResponse()
        }
        assertEquals(labelDao.getById(label.id)!!.pushNotificationText, dto.text)
    }

    @Test
    fun deleteLabel_works() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(adminAccessToken)
        } When {
            delete("v1/label/${label.publicId}")
        } Then {
            assertEmptyResponse()
        }
        assertNull(labelDao.getById(label.id))
        assertNull(userDao.getById(user.id))
    }

    @Test
    fun configureSelfTest_create_works() {
        val label = createLabel()
        Given {
            auth().oauth2(adminAccessToken)
            body(configureSelfTestDto)
        } When {
            post("v1/label/${label.publicId}/configure-self-test")
        } Then {
            assertEmptyResponse()
        }
    }

    @Test
    fun configureSelfTest_update_works() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        assertEquals(configureSelfTestDto.introduction?.text, selfTestService.get(label.id).introduction.text)

        Given {
            auth().oauth2(adminAccessToken)
            body(configureSelfTestDto.copy(introduction = ConfigureSelfTestIntroductionDto("updated-introduction")))
        } When {
            post("v1/label/${label.publicId}/configure-self-test")
        } Then {
            assertEmptyResponse()
        }
        assertEquals("updated-introduction", selfTestService.get(label.id).introduction.text)
    }

    @Test
    fun `configureSelfTest fails when boolean question has invalid answers`() {
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                personalAssistance = question(
                    "pa",
                    SelfTestQuestionType.BOOLEAN,
                    booleanValue(true),
                    booleanValue(true)
                )
            )
        )

        assertConfigureSelfTestFails(
            configureSelfTestDto(
                personalAssistance = question("pa", SelfTestQuestionType.BOOLEAN, booleanValue(false))
            )
        )
    }

    @Test
    fun `configureSelfTest fails when personal assistance question is not boolean`() {
        assertConfigureSelfTestFails(configureSelfTestDto(personalAssistance = sliderQuestion("pa")))
    }

    @Test
    fun `configureSelfTest fails when slider question misses value`() {
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                generalQuestions = listOf(
                    question(
                        "gen", SelfTestQuestionType.SLIDER,
                        sliderValue(1), sliderValue(3)
                    )
                )
            )
        )
    }

    @Test
    fun `configureSelfTest fails when question codes are not unique`() {
        val question = booleanQuestion("code")
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                generalQuestions = listOf(question),
                personalAssistance = question
            )
        )
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                problemAreas = listOf(
                    configureSelfTestProblemAreaDto("pa1", listOf(question)),
                    configureSelfTestProblemAreaDto("pa2", listOf(question)),
                )
            )
        )
    }

    @Test
    fun `configureSelfTest fails when answers are not unique`() {
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                generalQuestions = listOf(
                    singleChoiceQuestion(
                        "question",
                        "value",
                        "value"
                    )
                )
            )
        )
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                generalQuestions = listOf(
                    singleChoiceQuestion(
                        "question",
                        listOf(choiceValue("v1", value = "Text"), choiceValue("v2", value = "Text"))
                    )
                )
            )
        )
    }

    @Test
    fun `configureSelfTest fails when outcomes are incorrect`() {
        val questions = listOf(booleanQuestion("q"))
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                problemAreas = listOf(
                    configureSelfTestProblemAreaDto(
                        "pa", questions,
                        outcomes = listOf(outcome(SelfTestSeverity.MEDIUM), outcome(SelfTestSeverity.MEDIUM))
                    )
                )
            )
        )
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                problemAreas = listOf(
                    configureSelfTestProblemAreaDto(
                        "pa", questions,
                        outcomes = listOf(outcome(SelfTestSeverity.MEDIUM, personalAssistance = true))
                    )
                )
            )
        )
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                problemAreas = listOf(
                    configureSelfTestProblemAreaDto(
                        "pa", questions,
                        outcomes = listOf(
                            outcome(SelfTestSeverity.MEDIUM, personalAssistance = true),
                            outcome(SelfTestSeverity.SERIOUS, personalAssistance = false)
                        )
                    )
                )
            )
        )
        assertConfigureSelfTestFails(
            configureSelfTestDto(
                problemAreas = listOf(
                    configureSelfTestProblemAreaDto(
                        "pa", questions,
                        outcomes = listOf(
                            outcome(SelfTestSeverity.MEDIUM, personalAssistance = true),
                            outcome(SelfTestSeverity.MEDIUM, personalAssistance = false),
                            outcome(SelfTestSeverity.MEDIUM, personalAssistance = false)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Patch label works`() {
        val label = createLabel()
        val dto = UpdateLabelDto("updated-code-" + UUID.randomUUID(), "updated-name")
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            patch("v1/label/${label.publicId}")
        } Then {
            assertObjectResponse()
            body("id", equalTo(label.publicId.toString()))
            body("code", equalTo(dto.code))
            body("name", equalTo(dto.name))
        }

        val updatedLabel = labelDao.getById(label.id)!!
        assertEquals(dto.name, updatedLabel.name)
        assertEquals(dto.code, updatedLabel.code)
    }

    @Test
    fun `getServiceAvailability`() {
        val label = getOrCreateLabel(labelCode = "CZdirect")
        val user = createCustomer(label, "c1")

        // Wednesday
        clock.setTime(LocalDateTime.of(2022, 3, 16, 23, 0,0).atZone(ZoneId.of("Europe/Amsterdam")))

        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/label/${label.code}/service-available")
        } Then {
            body("serviceAvailable", equalTo(false))
            body("nextOpeningTime", equalTo("2022-03-17T08:00:00Z"))
        }
    }

    private fun assertConfigureSelfTestFails(
        dto: ConfigureSelfTestDto,
        validate: ValidatableResponse.(Label) -> Unit = {}
    ) {
        val label = createLabel()
        Given {
            auth().oauth2(adminAccessToken)
            body(dto)
        } When {
            post("v1/label/${label.publicId}/configure-self-test")
        } Then {
            assertErrorResponse(null)
            validate(label)
        }
    }
}
