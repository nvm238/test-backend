package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dto.GeneralPracticeCenterDto
import com.innovattic.medicinfo.database.dto.GeneralPracticeDto
import com.innovattic.medicinfo.database.dto.GeneralPracticePractitionerDto
import com.innovattic.medicinfo.logic.GeneralPracticeService
import com.innovattic.medicinfo.web.endpoint.Swagger
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/general-practice")
class GeneralPracticeEndpoint(
    private val service: GeneralPracticeService,
) : BaseEndpoint() {

    @GetMapping("/practice")
    @Operation(
        summary = "Get general practices",
        description = Swagger.PERMISSION_PUBLIC + "\n\nNote: This request is cached for 8 hours"
    )
    fun getGeneralPractices(
        @RequestParam("labelCode") labelCode: String?,
        @RequestParam("contracted") contracted: Boolean?
    ): ResponseEntity<List<GeneralPracticeDto>> {
        return ResponseEntity.ok(service.getGeneralPractices(labelCode, contracted ?: false))
    }

    @GetMapping("/practice/{code}/practitioner")
    @Operation(summary = "Get general practice practitioners", description = Swagger.PERMISSION_PUBLIC)
    fun getGeneralPracticePractitioner(
        @PathVariable("code") code: String,
        @RequestParam("labelCode") labelCode: String?
    ): ResponseEntity<List<GeneralPracticePractitionerDto>> {
        return ResponseEntity.ok(service.getGeneralPracticePractitioners(code, labelCode))
    }

    @GetMapping("/center")
    @Operation(
        summary = "Get general practice centers",
        description = Swagger.PERMISSION_PUBLIC + "\n\nNote: This request is cached for 8 hours"
    )
    fun getGeneralPracticeCenters(
        @RequestParam("labelCode") labelCode: String?,
        @RequestParam("contracted") contracted: Boolean?
    ): ResponseEntity<List<GeneralPracticeCenterDto>> {
        return ResponseEntity.ok(service.getGeneralPracticeCenters(labelCode, contracted ?: false))
    }
}
