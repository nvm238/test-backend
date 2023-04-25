package com.innovattic.medicinfo

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.innovattic.common.auth.JwtDto
import com.innovattic.common.client.InnovatticApiClient
import com.innovattic.medicinfo.database.dto.AdminDto
import com.innovattic.medicinfo.database.dto.ConversationDto
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.EmployeeDto
import com.innovattic.medicinfo.database.dto.LabelDto
import com.innovattic.medicinfo.dto.AnyUserDto
import com.innovattic.medicinfo.dto.NewAdminDto
import com.innovattic.medicinfo.dto.NewCustomerDto
import com.innovattic.medicinfo.dto.NewEmployeeDto
import com.innovattic.medicinfo.logic.UserService
import com.innovattic.medicinfo.logic.dto.PushNotificationDto
import org.springframework.http.HttpHeaders
import java.util.*

class MedicInfoApiClient(baseUrl: String) : InnovatticApiClient(
    baseUrl,
    configureMapper = { m -> m.propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE }
) {
    fun login(userId: UUID, apiKey: String) = target("v1/authentication/token/$userId")
        .header(HttpHeaders.AUTHORIZATION, "Digest $apiKey")
        .get()
        .handle<JwtDto>()
        .also { accessToken = it.accessToken }

    fun createAdmin(name: String, email: String) = request("v1/user/admin")
        .postJson(AdminDto(displayName = name, email = email))
        .handle<NewAdminDto>()

    fun createEmployee(name: String) = request("v1/user/employee")
        .postJson(EmployeeDto(displayName = name))
        .handle<NewEmployeeDto>()

    fun createCustomer(dto: CustomerDto) = request("v1/user/customer")
        .postJson(dto)
        .handle<NewCustomerDto>()

    fun registerCustomer(dto: CustomerDto) = target("v1/user/customer/register")
        .header(HttpHeaders.AUTHORIZATION, UserService.registerCustomerAuthorization(dto.labelId!!, dto.displayName!!))
        .postJson(dto)
        .handle<NewCustomerDto>()

    fun deleteUser(id: UUID) = request("v1/user/$id").delete().handleEmpty()

    fun sendPushNotification(userId: UUID, title: String, message: String, data: Map<String, Any>? = null) =
        request("v1/user/customer/$userId/send-push-notification")
            .postJson(PushNotificationDto(title, message, data))
            .handleEmpty()

    fun createLabel(name: String, code: String = name.lowercase().replace(' ', '-'), apiKey: String? = null) =
        request("v1/label")
            .postJson(LabelDto(code = code, name = name, fcmApiKey = apiKey))
            .handle<LabelDto>()

    fun getLabels(includeApiKeys: Boolean = false) = request("v1/label", "includeApiKeys" to includeApiKeys.toString())
        .get()
        .handle<List<LabelDto>>()

    fun getUsers(query: String? = null) = request("v1/user", "query" to query)
        .get()
        .handle<List<AnyUserDto>>()

    fun getConversation(id: UUID) = request("v1/conversation/$id")
        .get()
        .handle<ConversationDto>()
}
