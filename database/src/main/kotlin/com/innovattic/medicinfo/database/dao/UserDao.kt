package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.OrderHelper
import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.fetchPojos
import com.innovattic.common.database.fiqlQuery
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.common.database.zonedToDatabaseUtc
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dto.AdminDto
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.EmployeeDto
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.database.dto.UserDto
import com.innovattic.medicinfo.database.fiql.UserQueryParser
import com.innovattic.medicinfo.dbschema.Tables.API_KEY
import com.innovattic.medicinfo.dbschema.Tables.USER
import com.innovattic.medicinfo.dbschema.tables.UserView.USER_VIEW
import com.innovattic.medicinfo.dbschema.tables.pojos.ApiKey
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.dbschema.tables.pojos.UserView
import com.innovattic.medicinfo.dbschema.tables.records.UserRecord
import org.jetbrains.annotations.TestOnly
import org.jooq.DSLContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Component
class UserDao(private val context: DSLContext, private val clock: Clock) {
    private val userOrder = OrderHelper.Builder()
        .map("displayName", USER_VIEW.NAME)
        .map("role", USER_VIEW.ROLE)
        .map("email", USER_VIEW.EMAIL)
        .map("age", USER_VIEW.AGE)
        .default(USER_VIEW.ID)
        .build()

    fun getById(id: Int) = context.fetchOnePojo<User>(USER, USER.ID.eq(id))
    fun getByPublicId(id: UUID) = context.fetchOnePojo<User>(USER, USER.PUBLIC_ID.eq(id))
    fun getByEmail(labelId: Int?, email: String) =
        context.fetchOnePojo<User>(USER, USER.EMAIL.eq(email).and(labelCondition(labelId)))

    fun getByDeviceToken(labelId: Int?, deviceToken: String) = context.selectFrom(USER)
        .where(USER.DEVICE_TOKEN.eq(deviceToken).and(labelCondition(labelId)))
        .fetchPojos<User>()

    fun getList(query: String?, order: List<String>?): List<UserView> = context.selectFrom(USER_VIEW)
        .fiqlQuery(query, UserQueryParser)
        .orderBy(userOrder.parseOrders(order))
        .fetchInto(UserView::class.java)

    private fun labelCondition(labelId: Int?) = labelId?.let { USER.LABEL_ID.eq(it) } ?: USER.LABEL_ID.isNull

    fun createAdmin(dto: AdminDto) = createUser(dto) {
        it.email = dto.email
    }

    fun createEmployee(dto: EmployeeDto) = createUser(dto) {}

    fun createCustomer(labelId: Int, dto: CustomerDto) = createUser(dto) {
        it.labelId = labelId
        it.email = dto.email
        it.gender = dto.gender
        it.age = dto.age
        it.isInsured = dto.isInsured
        it.birthdate = dto.birthdate?.let { zonedToDatabaseUtc(it) }
        it.phoneNumber = dto.phoneNumber
        it.postalCode = dto.postalCode
        it.houseNumber = dto.houseNumber
    }

    fun updateCustomer(
        id: Int,
        displayName: String?,
        email: String?,
        gender: Gender?,
        age: Int?,
        isInsured: Boolean?,
        birthDate: LocalDateTime?,
        phoneNumber: String?,
        postalCode: String?,
        houseNumber: String?
    ): User {
        return context.updateRecord(USER) {
            it.name = displayName
            it.email = email
            it.gender = gender
            it.age = age
            it.isInsured = isInsured
            it.birthdate = birthDate
            it.phoneNumber = phoneNumber
            it.postalCode = postalCode
            it.houseNumber = houseNumber
        }.where(USER.ID.eq(id)).returningPojo()
    }

    fun updateCustomersBirthdate(
        id: Int,
        birthDate: LocalDateTime
    ): User = context.updateRecord(USER) {
        it.birthdate = birthDate
    }
        .where(USER.ID.eq(id))
        .returningPojo()

    fun updateCustomersOnboardingDetails(
        id: Int,
        entryType: String?,
        generalPractice: String?,
        generalPracticeAGBCode: String?,
        generalPracticeCenter: String?,
        generalPracticeCenterAGBCode: String?,
        holidayDestination: String?,
        shelterLocationId: String?,
        shelterLocationName: String?,
    ): User {
        return context.updateRecord(USER) {
            it.entryType = entryType
            it.generalPractice = generalPractice
            it.generalPracticeAgbCode = generalPracticeAGBCode
            it.generalPracticeCenter = generalPracticeCenter
            it.generalPracticeCenterAgbCode = generalPracticeCenterAGBCode
            it.holidayDestination = holidayDestination
            it.shelterLocationId = shelterLocationId
            it.shelterLocationName = shelterLocationName
            it.onboardingDetailsAdded = true
        }
            .where(USER.ID.eq(id))
            .returningPojo()
    }

    private fun createUser(dto: UserDto, additionalFields: (UserRecord) -> Unit): User {
        try {
            return context.insertRecord(USER) {
                it.created = databaseNow(clock)
                it.role = dto.role
                it.name = dto.displayName
                additionalFields(it)
            }.returningPojo()
        } catch (ignoreEx: DuplicateKeyException) {
            throw createResponseStatusException(code = ErrorCodes.DUPLICATE_EMAIL) { "User with that email address already exists" }
        }
    }

    fun delete(id: Int) {
        context.deleteFrom(USER).where(USER.ID.eq(id)).execute()
    }

    fun registerDeviceToken(id: Int, token: String, arn: String) {
        context.updateRecord(USER) {
            it.deviceToken = token
            it.snsEndpointArn = arn
        }.where(USER.ID.eq(id)).execute()
    }

    fun unregisterDeviceToken(id: Int) {
        context.updateRecord(USER) {
            it.deviceToken = null
            it.snsEndpointArn = null
        }.where(USER.ID.eq(id)).execute()
    }

    fun generateApiKey(userId: Int): String {
        return context.insertRecord(API_KEY) {
            it.userId = userId
            it.apiKey = UUID.randomUUID().toString()
        }.returningPojo<ApiKey>().apiKey
    }

    fun apiKeyExists(userId: UUID, apiKey: String): Boolean {
        return context.select(API_KEY.ID)
            .from(API_KEY)
            .join(USER).on(API_KEY.USER_ID.eq(USER.ID))
            .where(USER.PUBLIC_ID.eq(userId))
            .and(API_KEY.API_KEY_.eq(apiKey))
            .fetchOne() != null
    }

    @TestOnly
    fun clear(vararg exceptIds: Int) {
        context.deleteFrom(USER).where(USER.ID.notIn(*exceptIds.toTypedArray())).execute()
    }

    fun hasApiKey(userId: UUID): Boolean {
        return context.select(API_KEY.ID)
            .from(API_KEY)
            .join(USER).on(API_KEY.USER_ID.eq(USER.ID))
            .where(USER.PUBLIC_ID.eq(userId))
            .fetchOne() != null
    }

    fun removeApiKey(userId: UUID): Boolean {
        val user = getByPublicId(userId) ?: return false
        return context
            .deleteFrom(API_KEY)
            .where(API_KEY.USER_ID.eq(user.id))
            .execute() > 0
    }
}
