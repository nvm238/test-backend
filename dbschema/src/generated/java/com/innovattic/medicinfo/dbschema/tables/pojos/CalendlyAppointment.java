/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.pojos;


import com.innovattic.medicinfo.database.dto.AppointmentType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.14.16"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CalendlyAppointment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer         id;
    private UUID            publicId;
    private LocalDateTime   created;
    private String          uri;
    private Integer         customerId;
    private String          eventId;
    private String          inviteeId;
    private LocalDateTime   startTime;
    private LocalDateTime   endTime;
    private String          cancelReason;
    private LocalDateTime   canceledAt;
    private String          salesforceAppointmentId;
    private String          salesforceEventId;
    private AppointmentType appointmentType;
    private String          videoMeetingUrl;
    private String          requestFailReason;

    public CalendlyAppointment() {}

    public CalendlyAppointment(CalendlyAppointment value) {
        this.id = value.id;
        this.publicId = value.publicId;
        this.created = value.created;
        this.uri = value.uri;
        this.customerId = value.customerId;
        this.eventId = value.eventId;
        this.inviteeId = value.inviteeId;
        this.startTime = value.startTime;
        this.endTime = value.endTime;
        this.cancelReason = value.cancelReason;
        this.canceledAt = value.canceledAt;
        this.salesforceAppointmentId = value.salesforceAppointmentId;
        this.salesforceEventId = value.salesforceEventId;
        this.appointmentType = value.appointmentType;
        this.videoMeetingUrl = value.videoMeetingUrl;
        this.requestFailReason = value.requestFailReason;
    }

    public CalendlyAppointment(
        Integer         id,
        UUID            publicId,
        LocalDateTime   created,
        String          uri,
        Integer         customerId,
        String          eventId,
        String          inviteeId,
        LocalDateTime   startTime,
        LocalDateTime   endTime,
        String          cancelReason,
        LocalDateTime   canceledAt,
        String          salesforceAppointmentId,
        String          salesforceEventId,
        AppointmentType appointmentType,
        String          videoMeetingUrl,
        String          requestFailReason
    ) {
        this.id = id;
        this.publicId = publicId;
        this.created = created;
        this.uri = uri;
        this.customerId = customerId;
        this.eventId = eventId;
        this.inviteeId = inviteeId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cancelReason = cancelReason;
        this.canceledAt = canceledAt;
        this.salesforceAppointmentId = salesforceAppointmentId;
        this.salesforceEventId = salesforceEventId;
        this.appointmentType = appointmentType;
        this.videoMeetingUrl = videoMeetingUrl;
        this.requestFailReason = requestFailReason;
    }

    /**
     * Getter for <code>public.calendly_appointment.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.calendly_appointment.id</code>.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.calendly_appointment.public_id</code>.
     */
    public UUID getPublicId() {
        return this.publicId;
    }

    /**
     * Setter for <code>public.calendly_appointment.public_id</code>.
     */
    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    /**
     * Getter for <code>public.calendly_appointment.created</code>.
     */
    public LocalDateTime getCreated() {
        return this.created;
    }

    /**
     * Setter for <code>public.calendly_appointment.created</code>.
     */
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    /**
     * Getter for <code>public.calendly_appointment.uri</code>.
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Setter for <code>public.calendly_appointment.uri</code>.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Getter for <code>public.calendly_appointment.customer_id</code>.
     */
    public Integer getCustomerId() {
        return this.customerId;
    }

    /**
     * Setter for <code>public.calendly_appointment.customer_id</code>.
     */
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    /**
     * Getter for <code>public.calendly_appointment.event_id</code>.
     */
    public String getEventId() {
        return this.eventId;
    }

    /**
     * Setter for <code>public.calendly_appointment.event_id</code>.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Getter for <code>public.calendly_appointment.invitee_id</code>.
     */
    public String getInviteeId() {
        return this.inviteeId;
    }

    /**
     * Setter for <code>public.calendly_appointment.invitee_id</code>.
     */
    public void setInviteeId(String inviteeId) {
        this.inviteeId = inviteeId;
    }

    /**
     * Getter for <code>public.calendly_appointment.start_time</code>.
     */
    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Setter for <code>public.calendly_appointment.start_time</code>.
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Getter for <code>public.calendly_appointment.end_time</code>.
     */
    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Setter for <code>public.calendly_appointment.end_time</code>.
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Getter for <code>public.calendly_appointment.cancel_reason</code>.
     */
    public String getCancelReason() {
        return this.cancelReason;
    }

    /**
     * Setter for <code>public.calendly_appointment.cancel_reason</code>.
     */
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    /**
     * Getter for <code>public.calendly_appointment.canceled_at</code>.
     */
    public LocalDateTime getCanceledAt() {
        return this.canceledAt;
    }

    /**
     * Setter for <code>public.calendly_appointment.canceled_at</code>.
     */
    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    /**
     * Getter for <code>public.calendly_appointment.salesforce_appointment_id</code>.
     */
    public String getSalesforceAppointmentId() {
        return this.salesforceAppointmentId;
    }

    /**
     * Setter for <code>public.calendly_appointment.salesforce_appointment_id</code>.
     */
    public void setSalesforceAppointmentId(String salesforceAppointmentId) {
        this.salesforceAppointmentId = salesforceAppointmentId;
    }

    /**
     * Getter for <code>public.calendly_appointment.salesforce_event_id</code>.
     */
    public String getSalesforceEventId() {
        return this.salesforceEventId;
    }

    /**
     * Setter for <code>public.calendly_appointment.salesforce_event_id</code>.
     */
    public void setSalesforceEventId(String salesforceEventId) {
        this.salesforceEventId = salesforceEventId;
    }

    /**
     * Getter for <code>public.calendly_appointment.appointment_type</code>.
     */
    public AppointmentType getAppointmentType() {
        return this.appointmentType;
    }

    /**
     * Setter for <code>public.calendly_appointment.appointment_type</code>.
     */
    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    /**
     * Getter for <code>public.calendly_appointment.video_meeting_url</code>.
     */
    public String getVideoMeetingUrl() {
        return this.videoMeetingUrl;
    }

    /**
     * Setter for <code>public.calendly_appointment.video_meeting_url</code>.
     */
    public void setVideoMeetingUrl(String videoMeetingUrl) {
        this.videoMeetingUrl = videoMeetingUrl;
    }

    /**
     * Getter for <code>public.calendly_appointment.request_fail_reason</code>.
     */
    public String getRequestFailReason() {
        return this.requestFailReason;
    }

    /**
     * Setter for <code>public.calendly_appointment.request_fail_reason</code>.
     */
    public void setRequestFailReason(String requestFailReason) {
        this.requestFailReason = requestFailReason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CalendlyAppointment (");

        sb.append(id);
        sb.append(", ").append(publicId);
        sb.append(", ").append(created);
        sb.append(", ").append(uri);
        sb.append(", ").append(customerId);
        sb.append(", ").append(eventId);
        sb.append(", ").append(inviteeId);
        sb.append(", ").append(startTime);
        sb.append(", ").append(endTime);
        sb.append(", ").append(cancelReason);
        sb.append(", ").append(canceledAt);
        sb.append(", ").append(salesforceAppointmentId);
        sb.append(", ").append(salesforceEventId);
        sb.append(", ").append(appointmentType);
        sb.append(", ").append(videoMeetingUrl);
        sb.append(", ").append(requestFailReason);

        sb.append(")");
        return sb.toString();
    }
}
