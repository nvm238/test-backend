package com.innovattic.medicinfo.logic.calendly

import com.innovattic.medicinfo.logic.dto.calendly.AppointmentResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.AvailabilityResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.CalendlyUserDto
import com.innovattic.medicinfo.logic.dto.calendly.EventResponseWrapperDto
import com.innovattic.medicinfo.logic.dto.calendly.EventTypesResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.InviteeResponseDto
import com.innovattic.medicinfo.logic.dto.calendly.WebhookResponseDto

interface CalendlyApi {
    fun getUser(idUrl: String): CalendlyUserDto
    fun getOrganizationInfo(): CalendlyUserDto
    fun getEventTypes(organisationUrl: String): EventTypesResponseDto
    fun getEventTypesByNextPage(nextPage: String): EventTypesResponseDto
    fun getAvailability(eventTypeId: String, rangeStart: String, rangeEnd: String): AvailabilityResponseDto
    fun bookAppointment(timeslot: String, eventTypeId: String, fullName: String, email: String): AppointmentResponseDto
    fun cancelAppointment(inviteeId: String, reason: String?)
    fun getWebhooks(organizationUrl: String): WebhookResponseDto
    fun createWebhook(organizationUrl: String, signature: String, callbackUrl: String, events: List<String>, scope: String)
    fun getInvitee(eventTypeId: String, inviteeId: String): InviteeResponseDto
    fun getInvitee(url: String): InviteeResponseDto
    fun getEvent(url: String): EventResponseWrapperDto
    fun getWebhookSigningKey(): String
    fun validateSignature(header: String, dto: String)
}
