package com.innovattic.medicinfo.logic.dto.appointment

import com.innovattic.medicinfo.database.dto.AppointmentType
import java.time.ZonedDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ScheduleAppointmentDto(
    @field:NotEmpty val eventTypeId: String? = null,
    @field:NotNull val timeslot: ZonedDateTime? = null,
    val appointmentType: AppointmentType = AppointmentType.REGULAR
)
