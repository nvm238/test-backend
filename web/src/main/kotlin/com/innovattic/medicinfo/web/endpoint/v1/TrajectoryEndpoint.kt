package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.logic.TrajectoryService
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/trajectory")
class TrajectoryEndpoint(
    private val service: TrajectoryService,
) : BaseEndpoint() {
    @PostMapping("close")
    @Operation(summary = "Close trajectory", description = Swagger.PERMISSION_CUSTOMER)
    fun close() {
        verifyRole(UserRole.CUSTOMER)
        service.close(queryAuthenticatedUser())
    }
}
