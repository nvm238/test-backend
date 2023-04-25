package com.innovattic.medicinfo.web.endpoint

import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.web.BaseTriageEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class TriageOpeningHoursEndpointTest : BaseTriageEndpointTest() {

    @BeforeEach
    fun beforeEach() {
        doReturn(questionSchemaService.getQuestionSchema(0)).`when`(questionSchemaService).getLatestSchema()
        clock.reset()
    }

    @AfterEach
    fun afterEach() {
        clock.reset()
    }
/*
Test cases
- Default label
    - Default times - within opening times weekday
    - Default times - outside opening times weekday
    - Default times - within opening times weekend
    - Default times - outside opening times weekend
    - Overwrite weekday - within opening times
    - Overwrite weekday - outside opening times
    - Overwrite weekend day - within opening times
    - Overwrite weekend day - outside opening times
    - Triage closed all day
- Overwrite label
    - Default times - within opening times weekday
    - Default times - outside opening times weekday
    - Default times - within opening times weekend
    - Default times - outside opening times weekend
    - Overwrite weekday - within opening times
    - Overwrite weekday - outside opening times
    - Overwrite weekend day - within opening times
    - Overwrite weekend day - outside opening times
    - Triage closed all day
 */

    @Test
    fun `given default label, default times, when starting triage on Monday within service hours, expect new triage`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 18, 12, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given default label, default times, when starting triage on Monday outside service hours, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 18, 23, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
            body("conversation", equalTo(null))
        }
    }

    @Test
    fun `given default label, default times, when starting triage on Saturday within service hours, expect new triage`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 16, 12, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given default label, default times, when starting triage on Saturday outside service hours, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 16, 23, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given default label, overwrite Tuesday, when starting triage on Tuesday within service hours, expect new triage`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 19, 6, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given default label, overwrite Tuesday, when starting triage on second timeslot within service hours, expect new triage`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 19, 9, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given default label, overwrite Tuesday, when starting triage on Tuesday outside service hours, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 19, 5, 59,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given default label, overwrite Saturday, when starting triage on Saturday within service hours, expect new triage`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 16, 10, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given default label, overwrite Saturday, when starting triage on Saturday outside service hours, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 16, 20, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given default label, overwrite Wednesday, closed all day, when starting triage on Wednesday midnight, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 20, 0, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given default label, overwrite Wednesday, closed all day, when starting triage on Wednesday just before the end of day, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 20, 23, 59,59)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given default label, overwrite Wednesday, closed all day, when starting triage on Wednesday midday, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 20, 12, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given overwrite label, default times, when starting triage on Tuesday within service hours, expect new triage`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 19, 8, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given overwrite label, default times, when starting triage on Tuesday outside service hours, expect triage NOT_STARTED`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 19, 7, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given overwrite label, default times, when starting triage on Sunday within service hours, expect new triage`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 17, 10, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given overwrite label, default times, when starting triage on Sunday outside service hours, expect triage NOT_STARTED`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 17, 21, 41,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given overwrite label, overwrite Thursday, when starting triage on Thursday within service hours, expect new triage`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 21, 9, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given overwrite label, overwrite Thursday, when starting triage on Thursday outside service hours, expect triage NOT_STARTED`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 21, 8, 59,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given overwrite label, overwrite Saturday, when starting triage on Saturday within service hours, expect new triage`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 16, 11, 1,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given overwrite label, overwrite Saturday, when starting triage on Saturday outside service hours, expect triage NOT_STARTED`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 16, 10, 59,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given overwrite label, overwrite Friday, closed all day, when starting triage on Friday midnight, expect triage NOT_STARTED`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 22, 0, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given overwrite label, overwrite Friday, closed all day, when starting triage on Friday just before the end of day, expect triage NOT_STARTED`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 7, 22, 23, 59,59)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given default label, holiday, when starting triage in middle of the day, expect triage NEW`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 25, 12, 0, 0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given default label, holiday, when starting triage outside opening hours, expect triage NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 25, 23, 0, 0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given overwrite label, overwrite holiday, when starting triage during opening, expect triage NEW`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 25, 2, 1, 0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NEW"))
        }
    }

    @Test
    fun `given overwrite label, overwrite holiday, when starting triage during opening, expect triage NOT_STARTED`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 12, 25, 13, 1, 0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
        }
    }

    @Test
    fun `given service hours, when starting triage on weekday within service hours, expect new triage started and first profile question returned`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 3, 17, 10, 0,0)))
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
    fun `given valid service hours, when starting triage on weekday outside service hours, expect status NOT_STARTED`() {
        val label = createLabel()
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 3, 17, 23, 0,0)))
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("NOT_STARTED"))
            body("conversation", equalTo(null))
        }
    }

    @Test
    fun `given valid service hours and conversation with message, when starting triage on weekday outside service hours, expect status FINISHED`() {
        val label = createLabel()
        // NOTE: setTime is UTC zone, it means that in Europe/Amsterdam timezone time will be 23:00 (no daylight saving time)
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 3, 17, 22, 0,0)))
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
    fun `given service hours, when starting triage on weekend within service hours, expect new triage started and first profile question returned`() {
        val label = createLabel()
        // NOTE: setTime is UTC zone, it means that in Europe/Amsterdam timezone time will be 20:00 (no daylight saving time)
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 3, 19, 19, 0,0)))
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
    fun `given service hours, when starting triage on holiday within service hours, expect new triage started and first profile question returned`() {
        val label = createLabel()
        // NOTE: setTime is UTC zone, it means that in Europe/Amsterdam timezone time will be 20:00 (daylight saving time)
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 4, 18, 18, 0,0)))
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
    fun `given default service hours, when starting triage on weekend, expect new triage started and first profile question returned`() {
        val label = getOrCreateLabel("CZdirect")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 3, 20, 21, 0,0)))
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
    fun `given service hours and triage in progress, when starting triage outside service hours, expect next question returned`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 3, 17, 21, 0,0)))
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("IN_PROGRESS"))
            body("conversation.id", not(equalTo(null)))
            body("conversation.status", equalTo("open"))
            vaildateFemaleMedareaAnswers(true, startTriageQuestionObjectName)
        }
    }

    @Test
    fun `given service hours and finished triage, when starting triage outside service hours, expect FINISHED`() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        clock.setTime(convertTimeToAmsterdamTimezone(LocalDateTime.of(2022, 3, 17, 21, 0,0)))
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN)
        val triageStatus = triageStatusDao.createTriageStatus(user.id, questionnaireModel.version, conversation.id)
        saveAnswer(questionnaireModel.whoProfileQuestion, triageStatus.id, 1)
        triageStatusDao.endTriageStatus(triageStatus.id, TriageProgress.FINISHED)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/triage")
        } Then {
            body("triageStatus", equalTo("FINISHED"))
        }
    }

    private fun convertTimeToAmsterdamTimezone(dateTime: LocalDateTime): ZonedDateTime {
        return dateTime.atZone(ZoneId.of("Europe/Amsterdam"))
    }
}
