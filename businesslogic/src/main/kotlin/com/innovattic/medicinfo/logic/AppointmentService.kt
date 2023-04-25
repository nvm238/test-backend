package com.innovattic.medicinfo.logic

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.common.database.databaseUtcToZoned
import com.innovattic.common.database.zonedToDatabaseUtc
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dao.CalendlyAppointmentDao
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.AppointmentType
import com.innovattic.medicinfo.dbschema.tables.pojos.CalendlyAppointment
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.calendly.CalendlyApi
import com.innovattic.medicinfo.logic.dto.appointment.AppointmentDto
import com.innovattic.medicinfo.logic.dto.appointment.AvailabilityDto
import com.innovattic.medicinfo.logic.dto.appointment.CareTakerDto
import com.innovattic.medicinfo.logic.dto.appointment.SlotDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyCallbackDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyUserDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyEvent
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.apache.commons.lang3.exception.ExceptionUtils
import org.glassfish.jersey.internal.guava.CacheBuilder
import org.glassfish.jersey.internal.guava.CacheLoader
import org.glassfish.jersey.internal.guava.LoadingCache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class AppointmentService(
    private val calendlyApiClient: CalendlyApi,
    private val clock: Clock,
    private val dao: CalendlyAppointmentDao,
    private val labelDao: LabelDao,
    private val salesforceService: SalesforceService,
    private val userDao: UserDao,
    @Value("\${calendly.callback.url}") private val callbackUrl: String,
    @Value("\${calendly.dummy_email}") private val dummyEmail: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    val objectMapper = run {
        val jacksonMapper = jacksonObjectMapper()
        jacksonMapper.registerModule(JavaTimeModule())
        jacksonMapper.propertyNamingStrategy = PropertyNamingStrategies.SnakeCaseStrategy()
        jacksonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        jacksonMapper
    }

    private val userCache: LoadingCache<String, CalendlyUserDto> =
        CacheBuilder.newBuilder().expireAfterAccess(60L, TimeUnit.MINUTES).build(
            object : CacheLoader<String, CalendlyUserDto>() {
                override fun load(key: String?): CalendlyUserDto {
                    return calendlyApiClient.getUser(key!!)
                }
            }
        )

    private val eventCache: LoadingCache<Unit, List<CalendlyEvent>> =
        CacheBuilder.newBuilder().expireAfterAccess(10L, TimeUnit.MINUTES).build(
            object : CacheLoader<Unit, List<CalendlyEvent>>() {
                override fun load(key: Unit?): List<CalendlyEvent> {
                    val organization = calendlyApiClient.getOrganizationInfo().currentOrganization
                    return fetchAllEventTypes(organization)
                }
            }
        )

    @PostConstruct
    fun init() {
        try {
            val organizationInfo = calendlyApiClient.getOrganizationInfo()
            val webhooks = calendlyApiClient.getWebhooks(organizationInfo.currentOrganization)

            if (webhooks.collection.find { it.callbackUrl == callbackUrl } == null) {
                log.info("Setting up Calendly webhook for cancel events")
                calendlyApiClient.createWebhook(
                    organizationInfo.currentOrganization, calendlyApiClient.getWebhookSigningKey(),
                    callbackUrl, listOf("invitee.canceled"), "organization"
                )
            } else {
                log.info("Not setting up Calendly webhook - was already set up")
            }
        } catch (e: Exception) {
            log.warn("Could not setup Calendly webhook for cancel events", e)
        }
    }

    private fun fetchAllEventTypes(organizationUrl: String): List<CalendlyEvent> {
        val allEventTypes = mutableListOf<CalendlyEvent>()
        val eventTypes = calendlyApiClient.getEventTypes(organizationUrl)
        allEventTypes.addAll(eventTypes.collection)

        var currentPage = eventTypes.pagination.nextPage
        while (currentPage != null) {
            val nextEventTypes = calendlyApiClient.getEventTypesByNextPage(eventTypes.pagination.nextPage!!)

            currentPage = nextEventTypes.pagination.nextPage
            allEventTypes.addAll(nextEventTypes.collection)
        }

        return allEventTypes
    }

    fun getCareTakers(user: User, type: AppointmentType): List<CareTakerDto> {
        val labelCode = labelDao.getById(user.labelId)!!.code

        val internalNote = when (type) {
            AppointmentType.INTAKE -> "INT"
            AppointmentType.REGULAR -> labelCode.toString()
        }

        val events = eventCache[Unit].filter { it.internalNote == internalNote && it.active }

        val users = events.map { Pair(userCache[it.profile.owner], it) }.distinctBy { (user, _) ->
            user.uri
        }

        return users.mapNotNull { (user, event) ->
            try {
                val availability = getAvailability(event)
                CareTakerDto(
                    user.name,
                    event.descriptionHtml,
                    user.avatarUrl,
                    getId(event.uri),
                    event.duration,
                    availability
                )
            } catch (e: Exception) {
                log.warn("Failed to retrieve caretaker availability for event ${event.uri}", e)
                null
            }
        }
    }

    fun reschedule(user: User, previousId: UUID, eventTypeId: String, timeslot: ZonedDateTime): AppointmentDto {
        val previousAppointment = dao.get(previousId)
            ?: throw createResponseStatusException(code = ErrorCodes.APPOINTMENT_ALREADY_CANCELED) { "Appointment already canceled" }
        val appointment = schedule(user, timeslot, eventTypeId, previousAppointment.appointmentType, previousAppointment)
        cancelCalendly(previousAppointment.inviteeId, "Rescheduled to $timeslot")

        return appointment
    }

    fun retryScheduleSalesforceAppointment(user: User, appointmentPublicId: UUID): AppointmentDto {
        val appointment = getAppointmentDb(user, appointmentPublicId)
        if (appointment.canceledAt != null) {
            throw createResponseStatusException(code = ErrorCodes.APPOINTMENT_ALREADY_CANCELED) { "Cannot retry cancelled appointment" }
        }
        if (appointment.requestFailReason == null) {
            throw createResponseStatusException { "Cannot retry appointment that has not completed yet or completed successfully" }
        }
        arrangeSalesforceAppointment(appointment)

        return appointment.toDto()
    }

    private fun cancelCalendly(inviteeId: String, reason: String?) {
        try {
            calendlyApiClient.cancelAppointment(inviteeId, reason)
        } catch (_: Exception) {
            throw createResponseStatusException(code = ErrorCodes.APPOINTMENT_ALREADY_CANCELED) { "Appointment already canceled" }
        }
    }

    fun cancel(user: User, id: UUID, reason: String?): AppointmentDto {
        val appointmentDb = getAppointmentDb(user, id)
        cancelCalendly(appointmentDb.inviteeId, reason)
        cancelSalesforceAppointment(appointmentDb, true, reason)
        return cancelAppointment(appointmentDb.id, reason)
    }

    fun schedule(
        user: User,
        timeslot: ZonedDateTime,
        eventTypeId: String,
        appointmentType: AppointmentType,
        previousAppointment: CalendlyAppointment? = null
    ): AppointmentDto {
        try {
            val timeslotString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timeslot)
            val calendlyAppointmentResponse = calendlyApiClient.bookAppointment(
                timeslotString,
                eventTypeId,
                user.name,
                dummyEmail
            )
            val inviteeId = getId(calendlyAppointmentResponse.invitee.uri)
            val startTime = zonedToDatabaseUtc(calendlyAppointmentResponse.event.startTime)
            val endTime = zonedToDatabaseUtc(calendlyAppointmentResponse.event.endTime)
            val appointmentDb = if (previousAppointment != null) {
                dao.update(previousAppointment.id, calendlyAppointmentResponse.invitee.uri, eventTypeId, inviteeId, startTime, endTime)
            } else {
                dao.create(calendlyAppointmentResponse.invitee.uri, user.id, eventTypeId, inviteeId, startTime, endTime, appointmentType)
            }
            arrangeSalesforceAppointment(appointmentDb)

            return appointmentDb.toDto()
        } catch (exception: Exception) {
            log.warn("Exception while scheduling appointment: $exception")
            throw createResponseStatusException(code = ErrorCodes.APPOINTMENT_ALREADY_BOOKED) {
                "Appointment with slot $timeslot already booked"
            }
        }
    }

    private fun getEventAndContact(eventId: String): Pair<CalendlyEvent?, CalendlyUserDto?> {
        val event = eventCache[Unit].find {
            val eventTypeId = getId(it.uri)
            eventTypeId == eventId
        }

        val owner = event?.profile?.owner?.let { userCache[it] }
        return Pair(event, owner)
    }

    fun getAppointment(user: User, id: UUID): AppointmentDto {
        return getAppointmentDb(user, id).toDto()
    }

    fun getAppointmentPublicIdBySalesforceAppointmentId(id: String): UUID {
        val appointmentDto = dao.getBySalesforceAppointmentId(id)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Appointment with salesforce appointment id not found" }

        return appointmentDto.publicId
    }

    fun handleCallback(payloadString: String, header: String) {
        log.debug("Received callback request with payload: $payloadString and header $header")

        val dto = objectMapper.readValue(payloadString, CalendlyCallbackDto::class.java)
        calendlyApiClient.validateSignature(header, payloadString)

        val event = dto.payload
        val appointmentDb = dao.getByUri(event.uri)
            ?: return log.warn("Could not retrieve appointment with uri: ${event.uri}")

        if (event.rescheduled && event.newInvitee != null) {
            log.debug("Rescheduling appointment with id: ${appointmentDb.publicId}")
            val rescheduledInvitee = calendlyApiClient.getInvitee(event.newInvitee).resource
            val rescheduledEvent = calendlyApiClient.getEvent(rescheduledInvitee.event).resource
            val updatedAppointmentDb = dao.update(
                appointmentDb.id,
                rescheduledInvitee.uri,
                getId(rescheduledEvent.uri),
                getId(rescheduledInvitee.uri),
                zonedToDatabaseUtc(rescheduledEvent.startTime),
                zonedToDatabaseUtc(rescheduledEvent.endTime),
            )
            arrangeSalesforceAppointment(updatedAppointmentDb)
        } else if (event.status == "canceled" && appointmentDb.canceledAt == null) {
            log.debug("Cancelling appointment with id: ${appointmentDb.publicId}")
            cancelSalesforceAppointment(appointmentDb, false, event.cancellation.reason)
            cancelAppointment(appointmentDb.id, event.cancellation.reason)
            // TODO: Push notification
            // TODO: Can get user via val user = userDao.getById(appointmentDb.customerId) ?: return
        }
    }

    private fun cancelAppointment(appointmentId: Int, reason: String?): AppointmentDto =
        dao.cancel(appointmentId, reason).toDto()

    private fun arrangeSalesforceAppointment(
        appointment: CalendlyAppointment
    ) {
        val user = userDao.getById(appointment.customerId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with id ${appointment.customerId} not found" }

        val label = labelDao.getById(user.labelId)!!

        val (event, contact) = getEventAndContact(appointment.eventId)
        salesforceService.arrangeAppointmentAsync(
            user,
            appointment.salesforceAppointmentId,
            appointment.salesforceEventId,
            contact?.name ?: "Unknown",
            contact?.email ?: "Unknown",
            label.code,
            databaseUtcToZoned(appointment.startTime).toInstant(),
            databaseUtcToZoned(appointment.endTime).toInstant(),
            event?.name ?: "Unknown",
            appointment.salesforceAppointmentId != null,
            appointment.appointmentType == AppointmentType.INTAKE,
            { salesforceAppointment ->
                // This is outside of the normal web transaction, due to a different thread.
                dao.updateSalesforce(
                    appointment.id,
                    salesforceAppointment.appointmentId,
                    salesforceAppointment.eventId,
                    salesforceAppointment.videoMeetingUrl
                )
                log.info(
                    """
                AppointmentId: ${salesforceAppointment.appointmentId}
                EventId: ${salesforceAppointment.eventId}
                UserId: ${user.publicId}
                VideoUrl: ${salesforceAppointment.videoMeetingUrl}
            """.trimIndent()
                )
            },
            { exception ->
                dao.updateSalesforceFailReason(
                    appointment.id,
                    ExceptionUtils.getRootCauseMessage(exception) ?: "Something went wrong"
                )
                log.error(
                    "Request to arrange appointment in salesforce failed for " +
                    "user=${user.publicId} and appointmentId=${appointment.id}",
                        exception
                )
            }
        )
    }

    private fun cancelSalesforceAppointment(
        appointment: CalendlyAppointment,
        isCancelledByClient: Boolean,
        reason: String?
    ) {
        val user = userDao.getById(appointment.customerId)
            ?: return log.warn(
                "Could not find customer with id ${appointment.customerId} for cancellation " +
                "of appointment with id ${appointment.id}. SalesforceService not triggered."
            )

        salesforceService.cancelAppointmentAsync(
            user.publicId,
            appointment.salesforceAppointmentId,
            appointment.salesforceEventId,
            isCancelledByClient,
            reason.orEmpty()
        )
    }

    private fun getAppointmentDb(user: User, id: UUID): CalendlyAppointment {
        val appointmentDto = dao.get(id)
        if (appointmentDto == null || appointmentDto.customerId != user.id) {
            throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Appointment not found" }
        }

        return appointmentDto
    }

    private fun getAvailability(event: CalendlyEvent): List<AvailabilityDto> {
        val eventId = getId(event.uri)
        val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd")
        val today = ZonedDateTime.now(clock)
        val nextMonth = today.plusMonths(1L)
        val availability = calendlyApiClient.getAvailability(
            eventId,
            dateTimeFormatter.format(Date.from(today.toInstant())),
            dateTimeFormatter.format(Date.from(nextMonth.toInstant()))
        )

        return availability.days.map { day ->
            AvailabilityDto(
                day = day.date,
                slots = day.spots.map { spot ->
                    SlotDto(databaseUtcToZoned(zonedToDatabaseUtc(spot.startTime)))
                }
            )
        }
    }

    private fun getId(uri: String): String = uri.split("/").lastOrNull().orEmpty()

    private fun CalendlyAppointment.toDto(): AppointmentDto {
        return AppointmentDto(
            this.publicId,
            databaseUtcToZoned(this.startTime),
            databaseUtcToZoned(this.endTime),
            this.canceledAt?.let { databaseUtcToZoned(it) },
            this.cancelReason,
            this.videoMeetingUrl,
            this.appointmentType,
            !this.requestFailReason.isNullOrEmpty()
        )
    }
}
