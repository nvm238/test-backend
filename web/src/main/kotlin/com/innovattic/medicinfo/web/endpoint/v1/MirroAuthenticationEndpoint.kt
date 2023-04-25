package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.web.dto.MirroUrlDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.integration.mirro.MirroTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/mirro")
class MirroAuthenticationEndpoint(
    private val mirroTokenService: MirroTokenService
) : BaseEndpoint() {

    @GetMapping("/login/{blogId}")
    @Operation(
        summary = "Returns a link to Mirro containing signed JWT token",
        description = Swagger.PERMISSION_CUSTOMER
    )
    @Parameter(
        name = "blogId", description = "blogId of the module chosen by the customer. Value contained in GET /selftest endpoint response"
    )
    fun login(@PathVariable blogId: Int): ResponseEntity<MirroUrlDto> {
        return ResponseEntity.ok(mirroTokenService.createMirroSSOUrl(queryAuthenticatedUser(), blogId))
    }
}
