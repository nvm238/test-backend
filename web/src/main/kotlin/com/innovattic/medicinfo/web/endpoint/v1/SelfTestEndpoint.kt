package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.logic.SelfTestService
import com.innovattic.medicinfo.logic.dto.AppSelfTestDto
import com.innovattic.medicinfo.logic.dto.SelfTestAdviceDto
import com.innovattic.medicinfo.logic.dto.SelfTestAnswersDto
import com.innovattic.medicinfo.logic.dto.SelfTestProblemAreaDto
import com.innovattic.medicinfo.logic.dto.SelfTestResultsDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("v1/selftest")
class SelfTestEndpoint(private val selfTestService: SelfTestService) : BaseEndpoint() {
    @GetMapping
    @Operation(summary = "Get selftest", description = Swagger.PERMISSION_CUSTOMER)
    fun getSelfTest(): ResponseEntity<AppSelfTestDto> {
        verifyRole(UserRole.CUSTOMER)

        return ResponseEntity.ok(selfTestService.get(queryAuthenticatedUser()))
    }

    @PostMapping
    @Operation(summary = "Submit selftest answers", description = Swagger.PERMISSION_CUSTOMER)
    fun submitSelfTest(@RequestBody @Valid dto: SelfTestAnswersDto): ResponseEntity<SelfTestResultsDto> {
        verifyRole(UserRole.CUSTOMER)

        return ResponseEntity.ok(selfTestService.submitSelfTest(queryAuthenticatedUser(), dto))
    }

    @PostMapping("problem-area")
    @Operation(summary = "Select a problem area", description = Swagger.PERMISSION_CUSTOMER)
    fun selectProblemArea(@RequestBody @Valid dto: SelfTestProblemAreaDto): ResponseEntity<SelfTestAdviceDto> {
        verifyRole(UserRole.CUSTOMER)

        return ResponseEntity.ok(selfTestService.selectProblemArea(queryAuthenticatedUser(), dto))
    }

    @PostMapping("no-problem-area")
    @Operation(summary = "Select a problem area", description = Swagger.PERMISSION_CUSTOMER)
    fun selectNoProblemArea(): ResponseEntity<SelfTestAdviceDto> {
        verifyRole(UserRole.CUSTOMER)

        return ResponseEntity.ok(selfTestService.selectNoProblemArea(queryAuthenticatedUser()))
    }
}
