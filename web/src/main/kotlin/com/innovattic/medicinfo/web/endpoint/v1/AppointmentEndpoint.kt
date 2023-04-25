package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dto.AppointmentType
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.logic.AppointmentService
import com.innovattic.medicinfo.logic.dto.appointment.AppointmentDto
import com.innovattic.medicinfo.logic.dto.appointment.CancelAppointmentDto
import com.innovattic.medicinfo.logic.dto.appointment.CareTakerDto
import com.innovattic.medicinfo.logic.dto.appointment.RescheduleAppointmentDto
import com.innovattic.medicinfo.logic.dto.appointment.ScheduleAppointmentDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("v1/appointment")
class AppointmentEndpoint(
    private val appointmentService: AppointmentService,
) : BaseEndpoint() {

    @GetMapping("caretakers")
    @Operation(summary = "Get caretakers and their availability", description = Swagger.PERMISSION_CUSTOMER)
    @Parameter(
        name = "type", description = """Different type of appointments are supported, so the type should be specified by the caller"""
    )
    fun getCareTakers(
        @RequestParam(defaultValue = "REGULAR") type: AppointmentType
    ): List<CareTakerDto> {
        verifyRole(UserRole.CUSTOMER)

        return appointmentService.getCareTakers(queryAuthenticatedUser(), type)
    }

    @GetMapping("{id}")
    @Operation(summary = "Get an appointment", description = Swagger.PERMISSION_CUSTOMER)
    fun get(@PathVariable("id") id: UUID): AppointmentDto {
        verifyRole(UserRole.CUSTOMER)

        return appointmentService.getAppointment(queryAuthenticatedUser(), id)
    }

    @PostMapping("/schedule")
    @Operation(summary = "Schedule an appointment", description = Swagger.PERMISSION_CUSTOMER)
    fun schedule(
        @RequestBody @Valid dto: ScheduleAppointmentDto,
        @RequestParam(required = false) appointmentId: UUID?
    ): AppointmentDto {
        verifyRole(UserRole.CUSTOMER)

        if (appointmentId != null) {
            return appointmentService.retryScheduleSalesforceAppointment(queryAuthenticatedUser(), appointmentId)
        }
        return appointmentService.schedule(queryAuthenticatedUser(), dto.timeslot!!, dto.eventTypeId!!, dto.appointmentType)
    }

    @PostMapping("{id}/reschedule")
    @Operation(
        summary = "Reschedule an appointment, cancel previous appointment",
        description = Swagger.PERMISSION_CUSTOMER + "\nNote: Returns new appointment id"
    )
    fun reschedule(@PathVariable("id") id: UUID, @RequestBody @Valid dto: RescheduleAppointmentDto): AppointmentDto {
        verifyRole(UserRole.CUSTOMER)

        return appointmentService.reschedule(
            queryAuthenticatedUser(),
            id,
            dto.eventTypeId!!,
            dto.timeslot!!
        )
    }

    @PostMapping("{id}/cancel")
    @Operation(
        summary = "Cancel an appointment",
        description = Swagger.PERMISSION_CUSTOMER + "\nNote: Reason is optional."
    )
    fun cancel(@PathVariable("id") id: UUID, @RequestBody @Valid dto: CancelAppointmentDto): AppointmentDto {
        verifyRole(UserRole.CUSTOMER)

        return appointmentService.cancel(queryAuthenticatedUser(), id, dto.reason)
    }

    @PostMapping("callback")
    @Operation(summary = "Callback for Calendly")
    fun callback(
        @RequestBody dto: String,
        @RequestHeader("Calendly-Webhook-Signature") signature: String
    ) {
        appointmentService.handleCallback(dto, signature)
    }
}
