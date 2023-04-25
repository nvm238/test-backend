package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dto.ShelterLocationDto
import com.innovattic.medicinfo.logic.ShelterLocationService
import com.innovattic.medicinfo.web.endpoint.Swagger
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/shelter-location")
class ShelterLocationEndpoint(
    private val shelterLocationService: ShelterLocationService
) : BaseEndpoint() {

    @GetMapping
    @Operation(
        summary = "Get shelter locations",
        description = Swagger.PERMISSION_PUBLIC + "\n\nNote: This request is cached for 8 hours"
    )
    fun getShelterLocations(): ResponseEntity<List<ShelterLocationDto>> {
        return ResponseEntity.ok(shelterLocationService.getShelterLocations())
    }
}
