package com.innovattic.medicinfo.logic.dto.appointment

import java.time.ZonedDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class RescheduleAppointmentDto(
    @field:NotEmpty val eventTypeId: String? = null,
    @field:NotNull val timeslot: ZonedDateTime? = null,
)
