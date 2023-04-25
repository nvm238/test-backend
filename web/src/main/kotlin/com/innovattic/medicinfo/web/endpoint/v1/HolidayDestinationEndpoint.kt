package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dto.HolidayDestinationDto
import com.innovattic.medicinfo.logic.HolidayDestinationService
import com.innovattic.medicinfo.web.endpoint.Swagger
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/holiday-destination")
class HolidayDestinationEndpoint(
    private val service: HolidayDestinationService,
) : BaseEndpoint() {

    @GetMapping
    @Operation(
        summary = "Get holiday destinations",
        description = Swagger.PERMISSION_PUBLIC + "\n\nNote: This request is cached for 8 hours"
    )
    fun getHolidayDestinations(
        @RequestParam("labelCode") labelCode: String?
    ): ResponseEntity<List<HolidayDestinationDto>> {
        return ResponseEntity.ok(service.getHolidayDestinations(labelCode))
    }
}
