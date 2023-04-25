package com.innovattic.medicinfo.logic.calendly

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.logic.dto.calendly.AppointmentResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.AvailabilityResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyUserDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyUserResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.EventResponseWrapperDto
import com.innovattic.medicinfo.logic.dto.calendly.EventTypesResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.InviteeResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.WebhookResponseDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.util.UUID

class DevCalendlyApiClient : CalendlyApi {
    private val log = LoggerFactory.getLogger(javaClass)
    val objectMapper = run {
        val jacksonMapper = jacksonObjectMapper()
        jacksonMapper.registerModule(JavaTimeModule())
        jacksonMapper.propertyNamingStrategy = PropertyNamingStrategies.SnakeCaseStrategy()
        jacksonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        jacksonMapper
    }

    override fun getUser(idUrl: String): CalendlyUserDto {
        val json = """
            {
              "resource": {
                "avatar_url": null,
                "created_at": "2021-08-21T09:25:05.782541Z",
                "current_organization": "https://api.calendly.com/organizations/GCCC5BMKFEVGZYNY",
                "email": "test@innovattic.com",
                "name": "Calendly User",
                "scheduling_url": "https://calendly.com/calendlyuser",
                "slug": "calendlyuser",
                "timezone": "Europe/Berlin",
                "updated_at": "2021-08-23T10:59:01.616646Z",
                "uri": "https://api.calendly.com/users/HCAA3HOE5QV46VZ7"
              }
            }
        """.trimIndent()

        return objectMapper.readValue(json, CalendlyUserResponseDto::class.java).resource
    }

    override fun getOrganizationInfo(): CalendlyUserDto {
        val json = """
            {
              "resource": {
                "avatar_url": null,
                "created_at": "2021-08-21T09:25:05.782541Z",
                "current_organization": "https://api.calendly.com/organizations/GCCC5BMKFEVGZYNY",
                "email": "test@innovattic.com",
                "name": "Calendly User",
                "scheduling_url": "https://calendly.com/calendlyuser",
                "slug": "calendlyuser",
                "timezone": "Europe/Berlin",
                "updated_at": "2021-08-23T10:59:01.616646Z",
                "uri": "https://api.calendly.com/users/HCAA3HOE5QV46VZ7"
              }
            }
        """.trimIndent()

        return objectMapper.readValue(json, CalendlyUserResponseDto::class.java).resource
    }

    override fun getEventTypes(organisationUrl: String): EventTypesResponseDto {
        val json = """
            {
              "collection": [
                {
                  "active": true,
                  "color": "#8247f5",
                  "created_at": "2021-08-21T09:26:11.023300Z",
                  "custom_questions": [
                    {
                      "answer_choices": [],
                      "enabled": true,
                      "include_other": false,
                      "name": "Please share anything that will help prepare for our meeting.",
                      "position": 0,
                      "required": false,
                      "type": "text"
                    }
                  ],
                  "description_html": null,
                  "description_plain": null,
                  "duration": 30,
                  "internal_note": null,
                  "kind": "solo",
                  "name": "30 Minute Meeting",
                  "pooling_type": null,
                  "profile": {
                    "name": "Thomas van der Voort",
                    "owner": "https://api.calendly.com/users/HCAA3HOE5QV46VZ7",
                    "type": "User"
                  },
                  "scheduling_url": "https://calendly.com/thomasvdvoort/30min",
                  "secret": false,
                  "slug": "30min",
                  "type": "StandardEventType",
                  "updated_at": "2021-08-21T09:26:11.023300Z",
                  "uri": "https://api.calendly.com/event_types/AFIFFTQ4QCVCQI76"
                },
                {
                  "active": true,
                  "color": "#ff4f00",
                  "created_at": "2021-08-21T09:26:11.060693Z",
                  "custom_questions": [
                    {
                      "answer_choices": [],
                      "enabled": true,
                      "include_other": false,
                      "name": "Please share anything that will help prepare for our meeting.",
                      "position": 0,
                      "required": false,
                      "type": "text"
                    }
                  ],
                  "description_html": null,
                  "description_plain": null,
                  "duration": 60,
                  "internal_note": null,
                  "kind": "solo",
                  "name": "60 Minute Meeting",
                  "pooling_type": null,
                  "profile": {
                    "name": "Thomas van der Voort",
                    "owner": "https://api.calendly.com/users/HCAA3HOE5QV46VZ7",
                    "type": "User"
                  },
                  "scheduling_url": "https://calendly.com/thomasvdvoort/60min",
                  "secret": false,
                  "slug": "60min",
                  "type": "StandardEventType",
                  "updated_at": "2021-08-21T09:26:11.060693Z",
                  "uri": "https://api.calendly.com/event_types/FFIABXU4TDWAXVBA"
                },
                {
                  "active": true,
                  "color": "#f8e436",
                  "created_at": "2021-08-21T09:26:10.985823Z",
                  "custom_questions": [
                    {
                      "answer_choices": [],
                      "enabled": true,
                      "include_other": false,
                      "name": "Please share anything that will help prepare for our meeting.",
                      "position": 0,
                      "required": false,
                      "type": "text"
                    }
                  ],
                  "description_html": "<p>Hi, ik ben gespecialiseerd in mentale problemen</p>",
                  "description_plain": "Hi, ik ben gespecialiseerd in mentale problemen",
                  "duration": 15,
                  "internal_note": "code-with-caretakers",
                  "kind": "solo",
                  "name": "Mentale Coach",
                  "pooling_type": null,
                  "profile": {
                    "name": "Thomas van der Voort",
                    "owner": "https://api.calendly.com/users/HCAA3HOE5QV46VZ7",
                    "type": "User"
                  },
                  "scheduling_url": "https://calendly.com/thomasvdvoort/15min",
                  "secret": false,
                  "slug": "15min",
                  "type": "StandardEventType",
                  "updated_at": "2021-08-23T11:01:00.171659Z",
                  "uri": "https://api.calendly.com/event_types/HCPHEVTYWFQFQK6T"
                },
                {
                  "active": true,
                  "color": "#f8e436",
                  "created_at": "2021-08-21T09:26:10.985823Z",
                  "custom_questions": [
                    {
                      "answer_choices": [],
                      "enabled": true,
                      "include_other": false,
                      "name": "Please share anything that will help prepare for our meeting.",
                      "position": 0,
                      "required": false,
                      "type": "text"
                    }
                  ],
                  "description_html": "<p>Hi, ik ben gespecialiseerd in mentale problemen</p>",
                  "description_plain": "Hi, ik ben gespecialiseerd in mentale problemen",
                  "duration": 15,
                  "internal_note": "code-with-failing-caretakers",
                  "kind": "solo",
                  "name": "Mentale Coach",
                  "pooling_type": null,
                  "profile": {
                    "name": "Thomas van der Voort",
                    "owner": "https://api.calendly.com/users/HCAA3HOE5QV46VZ7",
                    "type": "User"
                  },
                  "scheduling_url": "https://calendly.com/thomasvdvoort/15min",
                  "secret": false,
                  "slug": "15min",
                  "type": "StandardEventType",
                  "updated_at": "2021-08-23T11:01:00.171659Z",
                  "uri": "https://api.calendly.com/event_types/$EVENT_TYPE_WITH_FAILING_CARETAKERS"
                }
              ],
              "pagination": {
                "count": 3,
                "next_page": null
              }
            }
        """.trimIndent()

        return objectMapper.readValue(json, EventTypesResponseDto::class.java)
    }

    override fun getEventTypesByNextPage(nextPage: String): EventTypesResponseDto {
        return getEventTypes("")
    }

    override fun getAvailability(eventTypeId: String, rangeStart: String, rangeEnd: String): AvailabilityResponseDto {
        if (eventTypeId == EVENT_TYPE_WITH_FAILING_CARETAKERS) {
            throw createResponseStatusException(HttpStatus.NOT_FOUND) { "event type not found" }
        }

        val json = """
            {
              "invitee_publisher_error": false,
              "today": "2021-08-26",
              "availability_timezone": "Europe/Berlin",
              "days": [
                {
                  "date": "2021-10-01",
                  "status": "available",
                  "spots": [
                    {
                      "status": "available",
                      "start_time": "2021-10-01T10:30:00+02:00",
                      "invitees_remaining": 1
                    },
                    {
                      "status": "available",
                      "start_time": "2021-10-01T10:45:00+02:00",
                      "invitees_remaining": 1
                    },
                    {
                      "status": "available",
                      "start_time": "2021-10-01T11:00:00+02:00",
                      "invitees_remaining": 1
                    }
                  ],
                  "invitee_events": []
                }
              ],
              "diagnostic_data": null,
              "current_user": {
                "email": null,
                "locale": null,
                "date_notation": null,
                "time_notation": null,
                "avatar_url": null,
                "calendar_accounts": null,
                "is_pretending": false,
                "diagnostics": {
                  "available": false,
                  "enabled": false
                }
              }
            }
        """.trimIndent()

        return objectMapper.readValue(json, AvailabilityResponseDto::class.java)
    }

    override fun bookAppointment(
        timeslot: String,
        eventTypeId: String,
        fullName: String,
        email: String
    ): AppointmentResponseDto {
        val eventId = UUID.randomUUID()
        val json = """
            {
              "ab_options": {},
              "event": {
                "id": 273955540,
                "start_time": "2021-10-01T08:45:00Z",
                "end_time": "2021-10-01T09:00:00Z",
                "name": "Mentale Coach",
                "location": null,
                "event_type_kind": "solo",
                "active_notification_type": "email",
                "assigned_to": [
                  "Thomas van der Voort"
                ],
                "location_additional_info": null,
                "color": "#f8e436",
                "guests": [],
                "location_type": "custom",
                "scheduled_profile": {
                  "name": "Thomas van der Voort",
                  "avatar_url": null,
                  "logo_url": null,
                  "unbranded": false
                },
                "publishers_list": "Thomas van der Voort",
                "uri": "https://api.calendly.com/scheduled_events/$eventId"
              },
              "invitee": {
                "id": 317533513,
                "full_name": "TestUser",
                "email": "qwzopgqqyssxppmxtw@rffff.net",
                "timezone": "Europe/Berlin",
                "cancelled": false,
                "uuid": "BODU6Q4OAR5LKFTP",
                "first_name": null,
                "last_name": null,
                "locale": "en",
                "time_notation": "24h",
                "phone_number": null,
                "event_start": "2021-10-01T10:45:00+02:00",
                "event_end": "2021-10-01T11:00:00+02:00",
                "gcalendar_event_url": "https://calendly.com/invitees/BODU6Q4OAR5LKFTP/google",
                "icalendar_event_url": "https://calendly.com/invitees/BODU6Q4OAR5LKFTP/ics",
                "charge": null,
                "uri": "https://api.calendly.com/scheduled_events/$eventId/invitees/BODU6Q4OAR5LKFTP"
              }
            }
        """.trimIndent()

        return objectMapper.readValue(json, AppointmentResponseDto::class.java)
    }

    override fun cancelAppointment(inviteeId: String, reason: String?) {
    }

    override fun getWebhooks(organizationUrl: String): WebhookResponseDto {
        return WebhookResponseDto(emptyList())
    }

    override fun createWebhook(
        organizationUrl: String,
        signature: String,
        callbackUrl: String,
        events: List<String>,
        scope: String
    ) {
        log.info("Creating webhook: $organizationUrl - Events: $events")
    }

    override fun getInvitee(eventTypeId: String, inviteeId: String): InviteeResponseDto {
        return getInvitee("")
    }

    override fun getInvitee(url: String): InviteeResponseDto {
        val json = """
            {
              "resource": {
                "cancel_url": "https://calendly.com/cancellations/BODU6Q4OAR5LKFTP",
                "cancellation": {
                  "canceled_by": "TestUser",
                  "reason": null
                },
                "created_at": "2021-08-25T03:47:54.186688Z",
                "email": "qwzopgqqyssxppmxtw@rffff.net",
                "event": "https://api.calendly.com/scheduled_events/GMHEOIJCC6XQ4IKF",
                "first_name": null,
                "last_name": null,
                "name": "TestUser",
                "new_invitee": "https://api.calendly.com/scheduled_events/HNGBJIPBFZVZHKAX/invitees/AJGW3Q4KER5NTOCW",
                "old_invitee": null,
                "payment": null,
                "questions_and_answers": [],
                "reschedule_url": "https://calendly.com/reschedulings/BODU6Q4OAR5LKFTP",
                "rescheduled": true,
                "status": "canceled",
                "text_reminder_number": null,
                "timezone": "Europe/Berlin",
                "tracking": {
                  "utm_campaign": null,
                  "utm_source": null,
                  "utm_medium": null,
                  "utm_content": null,
                  "utm_term": null,
                  "salesforce_uuid": null
                },
                "updated_at": "2021-08-25T03:48:13.838980Z",
                "uri": "https://api.calendly.com/scheduled_events/GMHEOIJCC6XQ4IKF/invitees/BODU6Q4OAR5LKFTP"
              }
            }
        """.trimIndent()

        return objectMapper.readValue(json, InviteeResponseDto::class.java)
    }

    override fun getEvent(url: String): EventResponseWrapperDto {
        val json = """
            {
              "resource": {
                "created_at": "2021-08-24T06:55:22.619141Z",
                "end_time": "2021-10-01T09:00:00.000000Z",
                "event_guests": [],
                "event_memberships": [
                  {
                    "user": "https://api.calendly.com/users/HCAA3HOE5QV46VZ7"
                  }
                ],
                "event_type": "https://api.calendly.com/event_types/HCPHEVTYWFQFQK6T",
                "invitees_counter": {
                  "active": 1,
                  "limit": 1,
                  "total": 1
                },
                "location": {
                  "location": null,
                  "type": "custom"
                },
                "name": "Mentale Coach",
                "start_time": "2021-10-01T08:45:00.000000Z",
                "status": "active",
                "updated_at": "2021-08-24T06:55:22.619141Z",
                "uri": "https://api.calendly.com/scheduled_events/ANBEIGX3WX7ERBLE"
              }
            }
        """.trimIndent()

        return objectMapper.readValue(json, EventResponseWrapperDto::class.java)
    }

    override fun getWebhookSigningKey(): String = "callbackSignature"

    override fun validateSignature(header: String, dto: String) {
        return
    }

    companion object {
        const val EVENT_TYPE_WITH_FAILING_CARETAKERS = "EVENTTYPEWITHFAILINGCARETAKERS"
    }
}
