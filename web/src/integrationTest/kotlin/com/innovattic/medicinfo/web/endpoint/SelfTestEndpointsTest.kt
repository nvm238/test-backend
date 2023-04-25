package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.AppSelfTestDto
import com.innovattic.medicinfo.logic.dto.SelfTestAnswerDto
import com.innovattic.medicinfo.logic.dto.SelfTestAnswersDto
import com.innovattic.medicinfo.logic.dto.SelfTestProblemAreaDto
import com.innovattic.medicinfo.logic.dto.SelfTestSeverity
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SelfTestEndpointsTest : BaseEndpointTest() {

    @Test
    fun `Get SelfTest works for Customer`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/selftest")
        } Then {
            assertObjectResponse()
            body("generalAdviceForBarelyNotInsured", equalTo(selfTest.generalAdviceForBarelyNotInsured))
            body("generalAdviceForBarelyInsured", equalTo(selfTest.generalAdviceForBarelyInsured))
            body("introduction.text", equalTo(selfTest.introduction.text))
            body("problemAreaQuestion.title", equalTo(selfTest.problemAreaQuestion.title))
            body("problemAreaQuestion.description", equalTo(selfTest.problemAreaQuestion.description))
            body("problemAreaQuestion.problemAreas[0].mirro.link", equalTo(selfTest.problemAreaQuestion.problemAreas.first().mirro.link))
            body("problemAreaQuestion.problemAreas[0].mirro.blogId", equalTo(selfTest.problemAreaQuestion.problemAreas.first().mirro.blogId))
            body("problemAreaQuestion.problemAreas[0].code", equalTo(selfTest.problemAreaQuestion.problemAreas.first().code))
            body("problemAreaQuestion.problemAreas[0].name", equalTo(selfTest.problemAreaQuestion.problemAreas.first().name))
            with(selfTest.problemAreaQuestion.problemAreas.first().questions.first()) {
                body("problemAreaQuestion.problemAreas[0].questions[0].code", equalTo(code))
                body("problemAreaQuestion.problemAreas[0].questions[0].type", equalTo(type.name.lowercase()))
                body("problemAreaQuestion.problemAreas[0].questions[0].title", equalTo(title))
                body("problemAreaQuestion.problemAreas[0].questions[0].description", equalTo(description))
                body("problemAreaQuestion.problemAreas[0].questions[0].values", nullValue())
            }
            with(selfTest.problemAreaQuestion.problemAreas.first().questions[1]) {
                body("problemAreaQuestion.problemAreas[0].questions[1].code", equalTo(code))
                body("problemAreaQuestion.problemAreas[0].questions[1].type", equalTo(type.name.lowercase()))
                body("problemAreaQuestion.problemAreas[0].questions[1].title", equalTo(title))
                body("problemAreaQuestion.problemAreas[0].questions[1].description", equalTo(description))
                body("problemAreaQuestion.problemAreas[0].questions[1].values[0].code", equalTo(values!!.first().code))
                body("problemAreaQuestion.problemAreas[0].questions[1].values[0].title", equalTo(values!!.first().title))
                body("problemAreaQuestion.problemAreas[0].questions[1].values[1].code", equalTo(values!![1].code))
                body("problemAreaQuestion.problemAreas[0].questions[1].values[1].title", equalTo(values!![1].title))
                body("problemAreaQuestion.problemAreas[0].questions[1].values[2].code", equalTo(values!![2].code))
                body("problemAreaQuestion.problemAreas[0].questions[1].values[2].title", equalTo(values!![2].title))
            }
            with(selfTest.problemAreaQuestion.problemAreas.first().questions[2]) {
                body("problemAreaQuestion.problemAreas[0].questions[2].code", equalTo(code))
                body("problemAreaQuestion.problemAreas[0].questions[2].type", equalTo(type.name.lowercase()))
                body("problemAreaQuestion.problemAreas[0].questions[2].title", equalTo(title))
                body("problemAreaQuestion.problemAreas[0].questions[2].description", equalTo(description))
                body("problemAreaQuestion.problemAreas[0].questions[2].values", nullValue())
                body("problemAreaQuestion.problemAreas[0].questions[2].minimum", equalTo(minimum))
                body("problemAreaQuestion.problemAreas[0].questions[2].maximum", equalTo(maximum))
            }
            body("problemAreaQuestion.problemAreas[1].code", equalTo(selfTest.problemAreaQuestion.problemAreas[1].code))
            body("problemAreaQuestion.problemAreas[1].name", equalTo(selfTest.problemAreaQuestion.problemAreas[1].name))
            with(selfTest.problemAreaQuestion.problemAreas[1].questions.first()) {
                body("problemAreaQuestion.problemAreas[1].questions[0].code", equalTo(code))
                body("problemAreaQuestion.problemAreas[1].questions[0].type", equalTo(type.name.lowercase()))
                body("problemAreaQuestion.problemAreas[1].questions[0].title", equalTo(title))
                body("problemAreaQuestion.problemAreas[1].questions[0].description", equalTo(description))
                body("problemAreaQuestion.problemAreas[1].questions[0].values", nullValue())
            }
            with(selfTest.generalQuestions.first()) {
                body("generalQuestions[0].code", equalTo(code))
                body("generalQuestions[0].type", equalTo(type.name.lowercase()))
                body("generalQuestions[0].title", equalTo(title))
                body("generalQuestions[0].description", equalTo(description))
                body("generalQuestions[0].values[0].code", equalTo(values!!.first().code))
                body("generalQuestions[0].values[1].code", equalTo(values!![1].code))
            }
            with(selfTest.personalAssistance) {
                body("personalAssistance.code", equalTo(code))
                body("personalAssistance.type", equalTo(type.name.lowercase()))
                body("personalAssistance.title", equalTo(title))
                body("personalAssistance.description", equalTo(description))
                body("personalAssistance.values", nullValue())
            }
        }
    }

    @Test
    fun `Submit SelfTest eligible for PersonalAssistance for Customer with Insurance`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1", insured = true)

        submitSelfTest(
            user,
            SelfTestAnswersDto(
                selectedProblemAreaCodes = selfTest.problemAreaQuestion.problemAreas.map { it.code },
                answers = listOf(SelfTestAnswerDto("code-problem-area-question-1", booleanAnswer = true))
            )
        ) {
            assertObjectResponse()
            body("eligibleForPersonalAssistance", equalTo(true))
        }
    }

    @Test
    fun `Submit SelfTest not eligble for PersonalAssistance for Customer without Insurance`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1", insured = false)

        submitSelfTest(
            user,
            SelfTestAnswersDto(
                selectedProblemAreaCodes = selfTest.problemAreaQuestion.problemAreas.map { it.code },
                answers = listOf(SelfTestAnswerDto("code-problem-area-question-1", booleanAnswer = true))
            )
        ) {
            assertObjectResponse()
            body("eligibleForPersonalAssistance", equalTo(true))
        }
    }

    @Test
    fun `Submit SelfTest advices severity SERIOUS for Customer`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1")

        submitSelfTest(
            user,
            SelfTestAnswersDto(
                selectedProblemAreaCodes = selfTest.problemAreaQuestion.problemAreas.map { it.code },
                answers = listOf(
                    // This question is worth 1 point
                    SelfTestAnswerDto(
                        "code-problem-area-question-1",
                        booleanAnswer = true
                    ),
                    // This question is worth 3 points
                    SelfTestAnswerDto(
                        "code-problem-area-question-2",
                        singleChoiceAnswerCode = "code-single-choice-choice-3"
                    ),
                    // This question is worth 4 points
                    SelfTestAnswerDto(
                        "code-problem-area-question-3",
                        sliderAnswer = 4
                    )
                )
            )
        ) {
            assertObjectResponse()
            body("results[0].problemAreaCode", equalTo(selfTest.problemAreaQuestion.problemAreas.first().code))

            // Average of (1 + 3 + 4)/3 = 2.7
            body("results[0].severity", equalTo(SelfTestSeverity.SERIOUS.value))
        }
    }

    @Test
    fun `Submit SelfTest advices severity MEDIUM for Customer`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1")

        submitSelfTest(
            user,
            SelfTestAnswersDto(
                selectedProblemAreaCodes = selfTest.problemAreaQuestion.problemAreas.map { it.code },
                answers = listOf(
                    // This question is worth 0 points
                    SelfTestAnswerDto(
                        "code-problem-area-question-1",
                        booleanAnswer = false
                    ),
                    // This question is worth 3 points
                    SelfTestAnswerDto(
                        "code-problem-area-question-2",
                        singleChoiceAnswerCode = "code-single-choice-choice-3"
                    ),
                    // This question is worth 4 points
                    SelfTestAnswerDto(
                        "code-problem-area-question-3",
                        sliderAnswer = 4
                    )
                )
            )
        ) {
            assertObjectResponse()
            body("results[0].problemAreaCode", equalTo(selfTest.problemAreaQuestion.problemAreas.first().code))
            // Average of (0 + 3 + 4)/3 = 2.3
            body("results[0].severity", equalTo(SelfTestSeverity.MEDIUM.value))
        }
    }

    @Test
    fun `Submit SelfTest advices severity BARELY for Customer`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1")
        val selectedAreaCode = selfTest.problemAreaQuestion.problemAreas.first().code

        Given {
            body(SelfTestAnswersDto(
                selectedProblemAreaCodes = listOf(selectedAreaCode),
                answers = listOf(
                    // This question is worth 0 points
                    SelfTestAnswerDto("code-problem-area-question-1", booleanAnswer = false),
                    // This question is worth 1 points
                    SelfTestAnswerDto("code-problem-area-question-2", singleChoiceAnswerCode = "code-single-choice-choice-1"),
                    // This question is worth 2 points
                    SelfTestAnswerDto("code-problem-area-question-3", sliderAnswer = 2))
            ))
            auth().oauth2(accessToken(user))
        } When {
            post("v1/selftest")
        } Then {
            assertObjectResponse()
            body("results[0].problemAreaCode", equalTo(selectedAreaCode))
            // Average of (0 + 1 + 2)/3 = 1.0
            body("results[0].severity", equalTo(SelfTestSeverity.BARELY.name.lowercase()))
        }
    }

    @Test
    fun `Submit SelfTest overwrites previous value`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1")
        val firstValue = SelfTestAnswersDto(
            selectedProblemAreaCodes = listOf(selfTest.problemAreaQuestion.problemAreas.first().code),
            answers = listOf(
                SelfTestAnswerDto(
                    "code-problem-area-question-1",
                    booleanAnswer = true
                )
            )
        )

        val overwrittenValue = SelfTestAnswersDto(
            selectedProblemAreaCodes = listOf(selfTest.problemAreaQuestion.problemAreas.first().code),
            answers = listOf(
                SelfTestAnswerDto(
                    "code-problem-area-2-question-1",
                    booleanAnswer = true
                )
            )
        )
        assertNull(userSelfTestResultService.get(user.id))
        selfTestService.submitSelfTest(user, firstValue)
        assertEquals(firstValue, userSelfTestResultService.get(user.id))
        selfTestService.submitSelfTest(user, overwrittenValue)
        assertEquals(overwrittenValue, userSelfTestResultService.get(user.id))
    }

    @Test
    fun `Submit SelfTest advices severity LIGHT and SERIOUS for Customer`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1")

        Given {
            body(
                SelfTestAnswersDto(
                    selectedProblemAreaCodes = selfTest.problemAreaQuestion.problemAreas.map { it.code },
                    answers = listOf(
                        // This question is worth 1 points
                        SelfTestAnswerDto(
                            "code-problem-area-question-1",
                            booleanAnswer = true
                        ),
                        // This question is worth 1 points
                        SelfTestAnswerDto(
                            "code-problem-area-question-2",
                            singleChoiceAnswerCode = "code-single-choice-choice-1"
                        ),
                        // This question is worth 3 points
                        SelfTestAnswerDto(
                            "code-problem-area-question-3",
                            sliderAnswer = 3
                        ),
                        // This question is worth 5 points
                        SelfTestAnswerDto(
                            "code-problem-area-2-question-1",
                            booleanAnswer = true
                        ),
                    )
                )
            )
            auth().oauth2(accessToken(user))
        } When {
            post("v1/selftest")
        } Then {
            assertObjectResponse()
            body("results[0].problemAreaCode", equalTo(selfTest.problemAreaQuestion.problemAreas.first().code))
            body("results[0].severity", equalTo(SelfTestSeverity.LIGHT.name.lowercase()))
            body("results[1].problemAreaCode", equalTo(selfTest.problemAreaQuestion.problemAreas[1].code))
            body("results[1].severity", equalTo(SelfTestSeverity.SERIOUS.name.lowercase()))
        }
    }

    @Test
    fun `Select Problem Area advices light advice`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1", insured = true)
        selectProblemArea(
            user, selfTest,
            SelfTestAnswerDto(
                "general-question-1",
                singleChoiceAnswerCode = "code-single-choice-choice-1"
            ),
            SelfTestProblemAreaDto(
                selfTest.problemAreaQuestion.problemAreas.first().code,
                true,
                SelfTestSeverity.LIGHT
            )
        ) {
            assertObjectResponse()
            body("adviceText", equalTo("light-advice"))
            body("chatText", equalTo("light-chat"))
            body("mirro.use", equalTo(false))
            body("mirro.description", equalTo("not-needed"))
        }
    }

    @Test
    fun `Select Problem Area advices mild advice`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1", insured = true)
        selectProblemArea(
            user, selfTest,
            SelfTestAnswerDto(
                "general-question-1",
                singleChoiceAnswerCode = "code-single-choice-choice-2"
            ),
            SelfTestProblemAreaDto(
                selfTest.problemAreaQuestion.problemAreas.first().code,
                true,
                SelfTestSeverity.MEDIUM
            )
        ) {
            assertObjectResponse()
            body("adviceText", equalTo("mild-advice"))
            body("chatText", equalTo("mild-chat"))
            body("mirro.use", equalTo(false))
            body("mirro.description", nullValue())
        }
    }

    @Test
    fun `Select Problem Area advices LIGHT advice without insurance`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1", insured = false)
        selectProblemArea(
            user, selfTest,
            SelfTestAnswerDto(
                "general-question-1",
                singleChoiceAnswerCode = "code-single-choice-choice-2"
            ),
            SelfTestProblemAreaDto(
                selfTest.problemAreaQuestion.problemAreas.first().code,
                true,
                SelfTestSeverity.LIGHT
            )
        ) {
            assertObjectResponse()
            body("adviceText", equalTo("light-advice"))
            body("chatText", equalTo("light-chat"))
            body("mirro.use", equalTo(false))
            body("mirro.description", equalTo("not-needed"))
        }
    }

    @Test
    fun `Select Problem Area advices serious advice with personal assistance`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1", insured = true)
        selectProblemArea(
            user, selfTest,
            SelfTestAnswerDto(
                "general-question-1",
                singleChoiceAnswerCode = "code-single-choice-choice-3"
            ),
            SelfTestProblemAreaDto(
                selfTest.problemAreaQuestion.problemAreas.first().code,
                true,
                SelfTestSeverity.SERIOUS
            )
        ) {
            assertObjectResponse()
            body("adviceText", equalTo("serious-advice-with-personal"))
            body("chatText", equalTo("serious-chat-with-personal"))
            body("mirro.use", equalTo(false))
            body("mirro.description", equalTo("any-reason"))
        }
    }

    @Test
    fun `Select Problem Area advices serious advice without personal assistance`() {
        val label = createLabel()
        selfTestService.createOrUpdate(label.publicId, configureSelfTestDto)
        val selfTest = selfTestService.get(label.id)
        val user = createCustomer(label, "c1", insured = true)
        selectProblemArea(
            user, selfTest,
            SelfTestAnswerDto(
                "general-question-1",
                singleChoiceAnswerCode = "code-single-choice-choice-3"
            ),
            SelfTestProblemAreaDto(
                selfTest.problemAreaQuestion.problemAreas.first().code,
                false,
                SelfTestSeverity.SERIOUS
            )
        ) {
            assertObjectResponse()
            body("adviceText", equalTo("serious-advice-with-mirro"))
            body("chatText", equalTo("serious-chat-with-mirro"))
            body("mirro.use", equalTo(true))
            body("mirro.description", nullValue())
        }
    }

    private fun selectProblemArea(
        user: User,
        selfTest: AppSelfTestDto,
        generalQuestion: SelfTestAnswerDto,
        dto: SelfTestProblemAreaDto,
        assertion: ValidatableResponse.() -> Unit
    ) {
        selfTestService.submitSelfTest(
            user, SelfTestAnswersDto(
                selectedProblemAreaCodes = selfTest.problemAreaQuestion.problemAreas.map { it.code },
                answers = listOf(
                    // This question is worth 0 points
                    SelfTestAnswerDto("code-problem-area-question-1", booleanAnswer = false),
                    // This question is worth 1 points
                    SelfTestAnswerDto("code-problem-area-question-2", singleChoiceAnswerCode = "code-single-choice-choice-1"),
                    // This question is worth 3 points
                    SelfTestAnswerDto("code-problem-area-question-3", sliderAnswer = 3),
                    generalQuestion
                )
            )
        )

        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/selftest/problem-area")
        } Then {
            assertion()
        }
    }

    private fun submitSelfTest(user: User, dto: SelfTestAnswersDto, assertion: ValidatableResponse.() -> Unit) {
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/selftest")
        } Then {
            assertion()
        }
    }
}
