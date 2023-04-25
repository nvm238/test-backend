package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertListResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.medicinfo.database.dto.AppointmentType
import com.innovattic.medicinfo.logic.dto.appointment.CancelAppointmentDto
import com.innovattic.medicinfo.logic.dto.appointment.RescheduleAppointmentDto
import com.innovattic.medicinfo.logic.dto.appointment.ScheduleAppointmentDto
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.http.Header
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.time.ZonedDateTime

class AppointmentEndpointsTest : BaseEndpointTest() {

    @Test
    fun `Get CareTakers returns empty list`() {
        val label = getOrCreateLabel("code-with-no-caretakers")
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/appointment/caretakers")
        } Then {
            assertListResponse(0)
        }
    }

    @Test
    fun `Get CareTakers returns non empty list`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/appointment/caretakers")
        } Then {
            assertListResponse(1)
            body("[0].fullName", equalTo("Calendly User"))
            body("[0].description", equalTo("<p>Hi, ik ben gespecialiseerd in mentale problemen</p>"))
            body("[0].avatarUrl", nullValue())
            body("[0].eventTypeId", equalTo("HCPHEVTYWFQFQK6T"))
            body("[0].availabilities[0].day", equalTo("2021-10-01"))
            body("[0].availabilities[0].slots[0].time", equalTo("2021-10-01T08:30:00Z"))
            body("[0].availabilities[0].slots[1].time", equalTo("2021-10-01T08:45:00Z"))
            body("[0].availabilities[0].slots[2].time", equalTo("2021-10-01T09:00:00Z"))
        }
    }

    // Related JIRA issue: https://innovattic.atlassian.net/browse/MED-1444
    @Test
    fun `Get CareTakers does not fail when fetching caretaker availability fails`() {
        val label = getOrCreateLabel("code-with-failing-caretakers")
        val user = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/appointment/caretakers")
        } Then {
            assertListResponse(0)
        }
    }

    @Test
    fun `Get appointment returns not found for invalid user`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val appointment = appointmentService.schedule(user, ZonedDateTime.now(), "HCPHEVTYWFQFQK6T", AppointmentType.REGULAR)

        val differentUser = createCustomer(label, "c1")
        Given {
            auth().oauth2(accessToken(differentUser))
        } When {
            get("v1/appointment/${appointment.id}")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Get appointment returns found for valid user`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val appointment = appointmentService.schedule(user, ZonedDateTime.now(), "HCPHEVTYWFQFQK6T", AppointmentType.REGULAR)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/appointment/${appointment.id}")
        } Then {
            assertObjectResponse()
            body("id", equalTo(appointment.id.toString()))
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", nullValue())
            body("cancelReason", nullValue())
        }
    }

    @Test
    fun `Schedule appointment works for user`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val dto = ScheduleAppointmentDto(
            "HCPHEVTYWFQFQK6T",
            ZonedDateTime.now()
        )
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/appointment/schedule")
        } Then {
            assertObjectResponse()
            body("id", notNullValue())
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", nullValue())
            body("cancelReason", nullValue())
        }

        Mockito.verify(salesforceService, Mockito.atLeastOnce()).arrangeAppointmentAsync(
            any(),
            eq(null),
            eq(null),
            any(),
            any(),
            eq(label.code),
            eq(Instant.parse("2021-10-01T08:45:00Z")),
            eq(Instant.parse("2021-10-01T09:00:00Z")),
            any(),
            eq(false),
            eq(false),
            any(),
            any(),
        )
    }

    @Test
    fun `Schedule intake appointment works for user, expect video url on get appointment call`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val dto = ScheduleAppointmentDto(
            "HCPHEVTYWFQFQK6T",
            ZonedDateTime.now(),
            AppointmentType.INTAKE
        )
        doAnswer {
            ResponseEntity.ok("OK")
        }.`when`(salesforceClient).post<String>(any(), any(), any())
        var appointmentId = ""
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/appointment/schedule")
        } Then {
            assertObjectResponse()
            appointmentId = extract().body().path("id")
            body("id", notNullValue())
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", nullValue())
            body("cancelReason", nullValue())
            body("videoMeetingUrl", nullValue())
            body("appointmentType", equalTo(AppointmentType.INTAKE.name.lowercase()))
            body("creationFailed", equalTo(false))
        }

        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/appointment/$appointmentId")
        } Then {
            body("videoMeetingUrl", equalTo("https://video-url.com"))
            body("creationFailed", equalTo(false))
        }
    }

    @Test
    fun `Retry failed intake appointment works for user, expect videoUrl on get appointment call`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val dto = ScheduleAppointmentDto(
            "HCPHEVTYWFQFQK6T",
            ZonedDateTime.now(),
            AppointmentType.INTAKE
        )
        doAnswer {
            ResponseEntity.ok("OK")
        }.`when`(salesforceClient).post<String>(any(), any(), any())
        val calendlyAppointment = createCalendlyAppointment(
            user = user,
            appointmentType = AppointmentType.INTAKE,
            failReason = "THIS CALL FAILED WITH REASON"
        )
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/appointment/schedule?appointmentId=${calendlyAppointment.publicId}")
        } Then {
            assertObjectResponse()
            body("id", notNullValue())
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", nullValue())
            body("cancelReason", nullValue())
            body("videoMeetingUrl", nullValue())
            body("appointmentType", equalTo(AppointmentType.INTAKE.name.lowercase()))
            body("creationFailed", equalTo(true))
        }

        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/appointment/${calendlyAppointment.publicId}")
        } Then {
            body("videoMeetingUrl", equalTo("https://video-url.com"))
            body("creationFailed", equalTo(false))
        }
    }

    @Test
    fun `Schedule intake appointment fails with external error for user, expect creationFailed true on get appointment call`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val dto = ScheduleAppointmentDto(
            "HCPHEVTYWFQFQK6T",
            ZonedDateTime.now(),
            AppointmentType.INTAKE
        )
        doThrow(IllegalStateException("Fail on purpose")).`when`(salesforceClient).post<String>(any(), any(), any())
        var appointmentId = ""
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/appointment/schedule")
        } Then {
            assertObjectResponse()
            appointmentId = extract().body().path("id")
            body("id", notNullValue())
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", nullValue())
            body("cancelReason", nullValue())
            body("videoMeetingUrl", nullValue())
            body("appointmentType", equalTo(AppointmentType.INTAKE.name.lowercase()))
            body("creationFailed", equalTo(false))
        }

        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/appointment/$appointmentId")
        } Then {
            body("videoMeetingUrl", nullValue())
            body("creationFailed", equalTo(true))
        }
    }

    @Test
    fun `Cancel appointment without reason works for user`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val appointment = appointmentService.schedule(user, ZonedDateTime.now(), "HCPHEVTYWFQFQK6T", AppointmentType.REGULAR)
        val dto = CancelAppointmentDto()
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/appointment/${appointment.id}/cancel")
        } Then {
            assertObjectResponse()
            body("id", notNullValue())
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", notNullValue())
            body("cancelReason", nullValue())
        }

        Mockito.verify(salesforceService, Mockito.times(1)).cancelAppointmentAsync(
            eq(user.publicId),
            ArgumentMatchers.startsWith("appointment-id"),
            eq("appointment-event-id"),
            eq(true),
            eq("")
        )
    }

    @Test
    fun `Cancel appointment with reason works for user`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val appointment = appointmentService.schedule(user, ZonedDateTime.now(), "HCPHEVTYWFQFQK6T", AppointmentType.REGULAR)
        val dto = CancelAppointmentDto("Any reason")
        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/appointment/${appointment.id}/cancel")
        } Then {
            assertObjectResponse()
            body("id", notNullValue())
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", notNullValue())
            body("cancelReason", equalTo("Any reason"))
        }

        Mockito.verify(salesforceService, Mockito.times(1)).cancelAppointmentAsync(
            eq(user.publicId),
            ArgumentMatchers.startsWith("appointment-id"),
            eq("appointment-event-id"),
            eq(true),
            eq(dto.reason!!)
        )
    }

    @Test
    fun `Reschedule appointment works for user`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val appointment = appointmentService.schedule(user, ZonedDateTime.now(), "HCPHEVTYWFQFQK6T", AppointmentType.REGULAR)
        val dto = RescheduleAppointmentDto("HCPHEVTYWFQFQK6T", ZonedDateTime.now())

        // Verify previous schedule, as you can see reschedule is false.
        Mockito.verify(salesforceService, Mockito.atLeastOnce()).arrangeAppointmentAsync(
            any(),
            eq(null),
            eq(null),
            any(),
            any(),
            eq(label.code),
            eq(Instant.parse("2021-10-01T08:45:00Z")),
            eq(Instant.parse("2021-10-01T09:00:00Z")),
            any(),
            eq(false),
            eq(false),
            any(),
            any()
        )

        Given {
            auth().oauth2(accessToken(user))
            body(dto)
        } When {
            post("v1/appointment/${appointment.id}/reschedule")
        } Then {
            assertObjectResponse()
            body("id", equalTo(appointment.id.toString()))
            body("startTime", notNullValue())
            body("endTime", notNullValue())
            body("canceledAt", nullValue())
            body("cancelReason", nullValue())
        }

        // Verify schedule is true
        Mockito.verify(salesforceService, Mockito.times(1)).arrangeAppointmentAsync(
            any(),
            ArgumentMatchers.startsWith("appointment-id"),
            eq("appointment-event-id"),
            any(),
            any(),
            eq(label.code),
            eq(Instant.parse("2021-10-01T08:45:00Z")),
            eq(Instant.parse("2021-10-01T09:00:00Z")),
            any(),
            eq(true),
            eq(false),
            any(),
            any()
        )

        val updatedAppointment = appointmentService.getAppointment(user, appointment.id)
        assertNull(updatedAppointment.cancelReason)
        assertNull(updatedAppointment.canceledAt)
        assertEquals(updatedAppointment.id, appointment.id)
    }

    @Test
    fun `Cancel appointment callback works`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val appointment = createCalendlyAppointment(user)
        val payload = generateCallbackPayload(appointment.uri)

        Given {
            header(Header("Calendly-Webhook-Signature", "signature"))
            body(payload)
        } When {
            post("v1/appointment/callback")
        }

        Mockito.verify(salesforceService, Mockito.times(1)).cancelAppointmentAsync(
            eq(user.publicId),
            eq(appointment.salesforceAppointmentId),
            eq(appointment.salesforceEventId),
            eq(false),
            eq("cancel reason"),
        )
    }

    // Related JIRA Issue: https://innovattic.atlassian.net/browse/MED-1474
    @Test
    fun `Cancel appointment callback does not trigger salesforce when appointment is already canceled`() {
        val label = getOrCreateLabel("code-with-caretakers")
        val user = createCustomer(label, "c1")
        val appointment = createCalendlyAppointment(user)
        calendlyAppointmentDao.cancel(appointment.id, "cancel reason")
        val payload = generateCallbackPayload(appointment.uri)

        Given {
            header(Header("Calendly-Webhook-Signature", "signature"))
            body(payload)
        } When {
            post("v1/appointment/callback")
        }

        Mockito.verify(salesforceService, Mockito.never()).cancelAppointmentAsync(any(), any(), any(), any(), any())
    }

    private fun generateCallbackPayload(uri: String) : String {
        return """
            {
              "created_at": "2021-11-05T09:39:37.000000Z",
              "event": "invitee.canceled",
              "payload": {
                "cancel_url": "https://calendly.com/cancellations/9b8194f3-219a-4094-baca-8dca6394d96a",
                "cancellation": {
                  "canceled_by": "Test user",
                  "reason": "cancel reason"
                },
                "created_at": "2021-11-05T09:38:01.638334Z",
                "email": "email@example.com",
                "event": "https://api.calendly.com/scheduled_events/6291133f-8d05-4b3a-9141-0fb939afa5db",
                "first_name": null,
                "last_name": null,
                "name": "Test user",
                "new_invitee": null,
                "old_invitee": null,
                "payment": null,
                "reschedule_url": "https://calendly.com/reschedulings/9b8194f3-219a-4094-baca-8dca6394d96a",
                "rescheduled": false,
                "status": "canceled",
                "text_reminder_number": null,
                "timezone": "Europe/Berlin",
                "updated_at": "2021-11-05T09:39:37.427677Z",
                "uri": "$uri"
              }
            }
        """.trimIndent()
    }
}
