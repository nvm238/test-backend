package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.common.error.failResponseIf
import com.innovattic.medicinfo.database.dto.LabelDto
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.logic.SelfTestService
import com.innovattic.medicinfo.logic.LabelService
import com.innovattic.medicinfo.logic.dto.ConfigureSelfTestDto
import com.innovattic.medicinfo.web.dto.ApiKeyDto
import com.innovattic.medicinfo.database.dao.ConfigurePushNotificationMessageDto
import com.innovattic.medicinfo.logic.OnlineEmployeeService
import com.innovattic.medicinfo.logic.dto.ServiceAvailableDto
import com.innovattic.medicinfo.logic.dto.UpdateLabelDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("v1/label")
class LabelEndpoint(
    private val service: LabelService,
    private val environment: Environment,
    private val selfTestService: SelfTestService,
    private val onlineEmployeeService: OnlineEmployeeService
) : BaseEndpoint() {
    @GetMapping("code/{code}")
    @Operation(summary = "Get label by code", description = Swagger.PERMISSION_PUBLIC)
    fun getLabelByCode(@PathVariable("code") code: String): ResponseEntity<LabelDto> {
        return ResponseEntity.ok(service.getByCode(code))
    }

    @GetMapping
    @Operation(summary = "Get labels", description = Swagger.PERMISSION_EMPLOYEE)
    fun getLabels(
        @Parameter(description = "If set to `true`, and you are admin, also returns the FCM api keys. Defaults to `false`.")
        @RequestParam("includeApiKeys") includeApiKeys: Boolean?,
    ): ResponseEntity<List<LabelDto>> {
        val role = verifyRole(UserRole.ADMIN, UserRole.EMPLOYEE).getRole()
        return ResponseEntity.ok(service.getList(role == UserRole.ADMIN && (includeApiKeys ?: false)))
    }

    @GetMapping("my")
    @Operation(summary = "Get my label", description = Swagger.PERMISSION_CUSTOMER)
    fun getMyLabel(): ResponseEntity<LabelDto> {
        verifyRole(UserRole.CUSTOMER)
        return ResponseEntity.ok(service.get(queryAuthenticatedUser().labelId))
    }

    @PostMapping
    @Operation(summary = "Create label", description = Swagger.PERMISSION_ADMIN)
    fun createLabel(@Valid @RequestBody dto: LabelDto): ResponseEntity<LabelDto> {
        verifyRole(UserRole.ADMIN)
        return ResponseEntity.ok(service.createLabel(dto))
    }

    @PatchMapping("{id}")
    @Operation(summary = "Update label", description = Swagger.PERMISSION_ADMIN)
    fun patchLabel(@PathVariable("id") id: UUID, @Valid @RequestBody dto: UpdateLabelDto): ResponseEntity<LabelDto> {
        verifyRole(UserRole.ADMIN)
        return ResponseEntity.ok(service.updateLabel(id, dto, queryAuthenticatedUser()))
    }

    @PostMapping("{id}/configure-push-notifications")
    @Operation(summary = "Configure push notifications", description = Swagger.PERMISSION_ADMIN)
    fun configurePushNotifications(@PathVariable("id") id: UUID, @Valid @RequestBody dto: ApiKeyDto) {
        verifyRole(UserRole.ADMIN)
        failResponseIf(dto.fcmApiKey.isNullOrBlank()) { "fcmApiKey must be specified" }
        service.setupPushNotifications(id, dto.fcmApiKey, queryAuthenticatedUser())
    }

    @PostMapping("{id}/configure-push-notification-message")
    @Operation(
        summary = "Configure push notification text when a user receives a message",
        description = Swagger.PERMISSION_ADMIN
    )
    fun configurePushNotificationText(@PathVariable("id") id: UUID, @Valid @RequestBody dto: ConfigurePushNotificationMessageDto) {
        verifyRole(UserRole.ADMIN)
        service.setupPushNotificationText(dto, id, queryAuthenticatedUser())
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete label", description = Swagger.PERMISSION_ADMIN + "\n\nNot available in production.")
    fun deleteLabel(@PathVariable("id") id: UUID) {
        verifyRole(UserRole.ADMIN)
        failResponseIf(
            "prod" in environment.activeProfiles,
            HttpStatus.FORBIDDEN
        ) { "This endpoint is not available in production" }
        service.deleteLabel(id, queryAuthenticatedUser())
    }

    @PostMapping("{id}/configure-self-test")
    @Operation(summary = "Configure self test", description = Swagger.CONFIGURE_SELF_TEST)
    fun configureSelfTest(@PathVariable("id") id: UUID, @Valid @RequestBody dto: ConfigureSelfTestDto) {
        verifyRole(UserRole.ADMIN)
        selfTestService.createOrUpdate(id, dto)
    }

    @GetMapping("{code}/service-available")
    @Operation(summary = "Get service availability for current label", description = Swagger.PERMISSION_ALL)
    fun getServiceAvailability(@PathVariable("code") code: String): ResponseEntity<ServiceAvailableDto> {
        verifyRole(UserRole.CUSTOMER)
        val dto = onlineEmployeeService.getServiceAvailability(code)
        return ResponseEntity.ok(dto)
    }
}
