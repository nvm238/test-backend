package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.database.dto.AdminDto
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.EmployeeDto
import com.innovattic.medicinfo.database.dto.UserDto
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.logic.UserService
import com.innovattic.medicinfo.logic.dto.IdDataDto
import com.innovattic.medicinfo.logic.dto.IdDto
import com.innovattic.medicinfo.logic.dto.NewUserDto
import com.innovattic.medicinfo.logic.dto.PushNotificationDto
import com.innovattic.medicinfo.logic.dto.RegisterCustomerDto
import com.innovattic.medicinfo.logic.dto.UpdateCustomerDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyAuthentication
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("v1/user")
class UserEndpoint(
    private val service: UserService
) : BaseEndpoint() {

    @GetMapping
    @Operation(summary = "Get users", description = Swagger.PERMISSION_ADMIN + Swagger.USER_DTO)
    fun getList(
        @Parameter(
            description = Swagger.FIQL + "Supported fields: All except `deviceToken`, from all 3 user dto types",
            example = Swagger.FIQL_EXAMPLE
        )
        @RequestParam("query") query: String?,
        @RequestParam("order") order: List<String>?,
    ): ResponseEntity<List<UserDto>> {
        verifyRole(UserRole.ADMIN)
        return ResponseEntity.ok(service.getList(query, order))
    }

    @GetMapping("me")
    @Operation(summary = "Get authenticated user", description = Swagger.PERMISSION_ALL + Swagger.USER_DTO)
    fun getMyUser(): ResponseEntity<UserDto> {
        val authentication = verifyAuthentication()
        return ResponseEntity.ok(service.getUser(authentication.getUserId()))
    }

    @PostMapping("admin")
    @Operation(summary = "Create admin", description = Swagger.PERMISSION_ADMIN)
    fun createAdmin(@Valid @RequestBody dto: AdminDto): ResponseEntity<NewUserDto> {
        verifyRole(UserRole.ADMIN)
        return ResponseEntity.ok(service.createAdmin(dto))
    }

    @PostMapping("employee")
    @Operation(summary = "Create employee", description = Swagger.PERMISSION_ADMIN)
    fun createEmployee(@Valid @RequestBody dto: EmployeeDto): ResponseEntity<NewUserDto> {
        verifyRole(UserRole.ADMIN)
        return ResponseEntity.ok(service.createEmployee(dto))
    }

    @PostMapping("customer")
    @Operation(summary = "Create customer", description = Swagger.PERMISSION_ADMIN)
    fun createCustomer(@Valid @RequestBody dto: CustomerDto): ResponseEntity<NewUserDto> {
        verifyRole(UserRole.ADMIN)
        return ResponseEntity.ok(service.createCustomer(dto))
    }

    @PostMapping("customer/register")
    @Operation(
        summary = "Register customer",
        description = Swagger.REGISTER_CUSTOMER +
            "\n\n" +
            "The Authorization header consists of a Digestion of the label and name combination:\n" +
            "Digest + DigestUtils.sha256Hex(\"medicinfo-customer-registration;labelId;name\")" +
            "\n\n" +
            "When the customerOnboardingDetails field is present, this request will perform a COV check based on " +
            "the date of birth and BSN. A 200 response indicates a valid check, a 403 response indicates an invalid " +
            "check, a 500 response indicates a general error."
    )
    fun registerCustomer(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        @Valid @RequestBody dto: RegisterCustomerDto,
    ): ResponseEntity<NewUserDto> {
        return ResponseEntity.ok(service.registerCustomer(dto, authorization))
    }

    @PatchMapping("me")
    @Operation(summary = "Update authenticated customer", description = Swagger.PERMISSION_CUSTOMER)
    fun updateMyUser(@Valid @RequestBody dto: UpdateCustomerDto): ResponseEntity<CustomerDto> {
        verifyRole(UserRole.CUSTOMER)
        val result = service.patchCustomer(dto, queryAuthenticatedUser())
        return ResponseEntity.ok(result)
    }

    @PutMapping("{id}")
    @Operation(summary = "Update customer", description = Swagger.PERMISSION_ADMIN)
    fun updateCustomer(
        @PathVariable("id") id: UUID,
        @Valid @RequestBody dto: CustomerDto
    ): ResponseEntity<CustomerDto> {
        verifyRole(UserRole.ADMIN)
        val result = service.putCustomer(dto, id)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("{id}")
    @Operation(
        summary = "Delete user",
        description = Swagger.PERMISSION_ADMIN + "\n\n" +
            "NOTE: this deletes the users information permanently - like conversations, messages and attachments." +
            "\n\n" +
            " Be careful, as policies or laws may require data to be retained in the system."
    )
    fun deleteUser(@PathVariable("id") id: UUID) {
        verifyRole(UserRole.ADMIN)
        service.deleteUser(id, queryAuthenticatedUser())
    }

    @DeleteMapping("{id}/conversation")
    @Operation(
        summary = "Delete all users Conversation",
        description = Swagger.PERMISSION_ADMIN + "\n\n" +
            "NOTE: this deletes the conversation information permanently - including attachments." +
            "\n\n" +
            " Be careful, as policies or laws may require data to be retained in the system."
    )
    fun deleteUserConversation(@PathVariable("id") id: UUID) {
        verifyRole(UserRole.ADMIN)
        service.deleteUserConversations(id, queryAuthenticatedUser())
    }

    @DeleteMapping("{id}/attachments")
    @Operation(
        summary = "Delete all users attachments",
        description = Swagger.PERMISSION_ADMIN + "\n\n" +
            "NOTE: this deletes the user attachments permanently." +
            "\n\n" +
            " Be careful, as policies or laws may require data to be retained in the system."
    )
    fun deleteUserImages(@PathVariable("id") id: UUID) {
        verifyRole(UserRole.ADMIN)
        service.deleteUserAttachments(id, queryAuthenticatedUser())
    }

    @DeleteMapping("{id}/attachments/{attachmentId}")
    @Operation(
        summary = "Delete single user attachment",
        description = Swagger.PERMISSION_ADMIN + "\n\n" +
            "NOTE: this deletes provided user attachment permanently." +
            "\n\n" +
            " Be careful, as policies or laws may require data to be retained in the system."
    )
    fun deleteUserImage(@PathVariable("id") id: UUID, @PathVariable attachmentId: UUID) {
        verifyRole(UserRole.ADMIN)
        service.deleteUserAttachments(id, queryAuthenticatedUser(), listOf(attachmentId))
    }

    @PostMapping("customer/{id}/confirm-id-data")
    @Operation(
        summary = "Confirm ID data",
        description = Swagger.PERMISSION_CUSTOMER + "\n\nNote: firstName, lastName, bsn and birthDate are optional."
    )
    fun confirmIdData(@Valid @RequestBody dto: IdDataDto) {
        verifyRole(UserRole.CUSTOMER)
        service.sendIdData(queryAuthenticatedUser(), dto)
    }

    @PostMapping("customer/{id}/confirm-id")
    @Operation(summary = "Confirm ID only", description = Swagger.PERMISSION_CUSTOMER)
    fun confirmId(@Valid @RequestBody dto: IdDto) {
        verifyRole(UserRole.CUSTOMER)
        service.sendId(queryAuthenticatedUser(), dto)
    }

    @PostMapping("customer/{id}/send-push-notification")
    @Operation(summary = "Send push notification", description = Swagger.PERMISSION_ADMIN)
    fun sendPushNotification(@PathVariable id: UUID, @Valid @RequestBody dto: PushNotificationDto) {
        verifyRole(UserRole.ADMIN)
        service.sendPushNotification(id, dto)
    }
}
