package com.innovattic.medicinfo.logic.calendly

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.innovattic.common.client.InnovatticApiClient
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.logic.dto.calendly.AppointmentRequestDto
import com.innovattic.medicinfo.logic.dto.calendly.AppointmentResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.AvailabilityResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyUserDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyUserResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.CancellationRequest
import com.innovattic.medicinfo.logic.dto.calendly.CancellationRequestDto
import com.innovattic.medicinfo.logic.dto.calendly.CreateWebhookRequestDto
import com.innovattic.medicinfo.logic.dto.calendly.EventRequest
import com.innovattic.medicinfo.logic.dto.calendly.EventResponseWrapperDto
import com.innovattic.medicinfo.logic.dto.calendly.EventTypesResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.InviteeRequest
import com.innovattic.medicinfo.logic.dto.calendly.InviteeResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.LocationConfiguration
import com.innovattic.medicinfo.logic.dto.calendly.WebhookResponseDto
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.ws.rs.client.Invocation

/**
 * NOTE: if the password of a Calendly account is reset, *all api keys are removed*. So this will break our integrations
 * until a new api key is provisioned.
 */
class CalendlyApiClient(private val apiKey: String, private val webhookSigningKey: String) :
    InnovatticApiClient(
        null,
        configureMapper = { m ->
            m.registerModule(JavaTimeModule())
            m.propertyNamingStrategy = PropertyNamingStrategies.SnakeCaseStrategy()
            m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    ),
    CalendlyApi {

    override fun getUser(idUrl: String) = request(idUrl)
        .withAuth()
        .get()
        .handle<CalendlyUserResponseDto>().resource

    override fun getOrganizationInfo(): CalendlyUserDto = getUser("https://api.calendly.com/users/me")

    override fun getEventTypes(organisationUrl: String) =
        getEventTypesByNextPage("https://api.calendly.com/event_types?organization=$organisationUrl&count=$defaultPaginationCount")

    override fun getEventTypesByNextPage(nextPage: String) =
        request(nextPage)
            .withAuth()
            .get()
            .handle<EventTypesResponseDto>()

    override fun getAvailability(eventTypeId: String, rangeStart: String, rangeEnd: String): AvailabilityResponseDto =
        request(
            "https://calendly.com/api/booking/event_types/$eventTypeId/calendar/range",
            "timezone" to defaultCalendlyTimeZone,
            "diagnostics" to "false",
            "range_start" to rangeStart,
            "range_end" to rangeEnd
        )
            .get()
            .handle()

    override fun bookAppointment(
        timeslot: String,
        eventTypeId: String,
        fullName: String,
        email: String
    ): AppointmentResponseDto =
        request("https://calendly.com/api/booking/invitees")
            .postJson(
                AppointmentRequestDto(
                    event = EventRequest(LocationConfiguration("", "", ""), timeslot),
                    eventTypeUuid = eventTypeId,
                    invitee = InviteeRequest(
                        email = email,
                        fullName = fullName,
                        timeNotation = "24h",
                        timezone = defaultCalendlyTimeZone
                    )
                )
            )
            .handle()

    override fun cancelAppointment(inviteeId: String, reason: String?) {
        request("https://calendly.com/api/booking/cancellations/$inviteeId")
            .putJson(
                CancellationRequestDto(
                    cancellation = CancellationRequest(
                        reason.orEmpty(),
                        ""
                    )
                )
            )
            .handleEmpty()
    }

    override fun getWebhooks(organizationUrl: String): WebhookResponseDto =
        request(
            "https://api.calendly.com/webhook_subscriptions?" +
                "organization=$organizationUrl&count=$defaultPaginationCount&scope=organization"
        )
            .withAuth()
            .get()
            .handle()

    override fun createWebhook(
        organizationUrl: String,
        signature: String,
        callbackUrl: String,
        events: List<String>,
        scope: String
    ) {
        request("https://api.calendly.com/webhook_subscriptions")
            .withAuth()
            .postJson(
                CreateWebhookRequestDto(
                    callbackUrl,
                    events,
                    organizationUrl,
                    scope,
                    signature
                )
            )
            .handleEmpty()
    }

    override fun getInvitee(eventTypeId: String, inviteeId: String): InviteeResponseDto =
        getInvitee("https://api.calendly.com/scheduled_events/$eventTypeId/invitees/$inviteeId")

    override fun getInvitee(url: String): InviteeResponseDto =
        request(url)
            .withAuth()
            .get()
            .handle()

    override fun getEvent(url: String): EventResponseWrapperDto =
        request(url)
            .withAuth()
            .get()
            .handle()

    override fun getWebhookSigningKey(): String = webhookSigningKey

    private val log = LoggerFactory.getLogger(javaClass)

    override fun validateSignature(header: String, dto: String) {
        val headerMap = header.split(",").associate {
            val (left, right) = it.split("=")
            left to right
        }
        val signature = headerMap["v1"]
        val timestamp = headerMap["t"]
        if (timestamp.isNullOrEmpty() || signature.isNullOrEmpty()) {
            throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Invalid signature" }
        }

        val payload = "$timestamp.$dto"
        val actualSignature = createSignature(payload, this.webhookSigningKey)

        if (signature != actualSignature) {
            throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Invalid signature" }
        }

        log.debug("validateSignature success")
    }

    private fun createSignature(data: String, key: String): String {
        val sha256Hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        sha256Hmac.init(secretKey)

        return Hex.encodeHexString(sha256Hmac.doFinal(data.toByteArray()))
    }

    private fun Invocation.Builder.withAuth(): Invocation.Builder =
        this.header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")

    private val defaultCalendlyTimeZone = "Europe/Berlin"
    private val defaultPaginationCount = 100
}
