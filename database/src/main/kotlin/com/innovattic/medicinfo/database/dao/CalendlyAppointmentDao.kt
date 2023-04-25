package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.medicinfo.database.dto.AppointmentType
import com.innovattic.medicinfo.dbschema.Tables.CALENDLY_APPOINTMENT
import com.innovattic.medicinfo.dbschema.tables.pojos.CalendlyAppointment
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Component
class CalendlyAppointmentDao(private val context: DSLContext, private val clock: Clock) {

    fun get(id: UUID) =
        context.fetchOnePojo<CalendlyAppointment>(CALENDLY_APPOINTMENT, CALENDLY_APPOINTMENT.PUBLIC_ID.eq(id))

    fun getByUri(uri: String) =
        context.fetchOnePojo<CalendlyAppointment>(CALENDLY_APPOINTMENT, CALENDLY_APPOINTMENT.URI.eq(uri))

    fun getBySalesforceAppointmentId(id: String) =
        context.fetchOnePojo<CalendlyAppointment>(CALENDLY_APPOINTMENT, CALENDLY_APPOINTMENT.SALESFORCE_APPOINTMENT_ID.eq(id))

    fun create(
        uri: String,
        userId: Int,
        eventTypeId: String,
        inviteeId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        appointmentType: AppointmentType
    ) =
        context.insertRecord(CALENDLY_APPOINTMENT) {
            it.uri = uri
            it.customerId = userId
            it.created = databaseNow(clock)
            it.eventId = eventTypeId // Note: this is the Calendly event TYPE id
            it.inviteeId = inviteeId
            it.startTime = startTime
            it.endTime = endTime
            it.appointmentType = appointmentType
        }.returningPojo<CalendlyAppointment>()

    fun update(
        id: Int,
        uri: String,
        eventTypeId: String,
        inviteeId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): CalendlyAppointment {
        return context.updateRecord(CALENDLY_APPOINTMENT) {
            it.uri = uri
            it.eventId = eventTypeId // Note: this is the Calendly event TYPE id
            it.inviteeId = inviteeId
            it.startTime = startTime
            it.endTime = endTime
        }.where(CALENDLY_APPOINTMENT.ID.eq(id)).returningPojo()
    }

    fun updateSalesforce(
        id: Int,
        appointmentId: String,
        eventId: String,
        videoMeetingUrl: String,
    ): CalendlyAppointment {
        return context.updateRecord(CALENDLY_APPOINTMENT) {
            it.salesforceAppointmentId = appointmentId
            it.salesforceEventId = eventId
            it.videoMeetingUrl = videoMeetingUrl
            it.requestFailReason = null
        }.where(CALENDLY_APPOINTMENT.ID.eq(id)).returningPojo()
    }

    fun updateSalesforceFailReason(
        id: Int,
        requestFailReason: String
    ): CalendlyAppointment =
        context.updateRecord(CALENDLY_APPOINTMENT) {
            it.requestFailReason = requestFailReason
        }.where(CALENDLY_APPOINTMENT.ID.eq(id)).returningPojo()

    fun cancel(id: Int, reason: String?) =
        context.updateRecord(CALENDLY_APPOINTMENT) {
            it.canceledAt = databaseNow(clock)
            it.cancelReason = reason
        }.where(CALENDLY_APPOINTMENT.ID.eq(id)).returningPojo<CalendlyAppointment>()
}
