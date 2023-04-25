package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.logic.EmailOptInService
import com.innovattic.medicinfo.logic.dto.EmailOptInDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("v1/user/{id}/email-opt-in")
class EmailOptInEndpoint(
    private val service: EmailOptInService,
) : BaseEndpoint() {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @Operation(
        summary = "This endpoint can only be used for Medicinfo labels that are configured to support a trial period.",
        description = Swagger.PERMISSION_CUSTOMER
    )
    fun sendEmailOptIn(@PathVariable id: String, @RequestBody @Valid dto: EmailOptInDto) {
        log.info("Email-opt-in requested for user with id=$id and data=$dto")
        service.sendEmailOptIn(id, dto)
    }
}
