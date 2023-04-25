package com.innovattic.medicinfo.logic

import com.innovattic.common.database.databaseUtcToZoned
import com.innovattic.common.database.zonedToDatabaseUtc
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.error.failResponseIf
import com.innovattic.common.notification.PushNotificationPlatform
import com.innovattic.common.notification.PushNotificationService
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.PrivacyStatementDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.AdminDto
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.CustomerEntryType
import com.innovattic.medicinfo.database.dto.EmployeeDto
import com.innovattic.medicinfo.database.dto.UserDto
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.dbschema.tables.pojos.UserView
import com.innovattic.medicinfo.logic.dto.IdDataDto
import com.innovattic.medicinfo.logic.dto.IdDto
import com.innovattic.medicinfo.logic.dto.NewUserDto
import com.innovattic.medicinfo.logic.dto.PrivacyStatementDto
import com.innovattic.medicinfo.logic.dto.PushNotificationDto
import com.innovattic.medicinfo.logic.dto.RegisterCustomerDto
import com.innovattic.medicinfo.logic.dto.UpdateCustomerDto
import com.innovattic.medicinfo.logic.dto.salesforce.UserContactDataSalesforceResponse
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class UserService(
    private val dao: UserDao,
    private val labelDao: LabelDao,
    private val conversationDao: ConversationDao,
    private val notificationService: PushNotificationService,
    private val salesforceService: SalesforceService,
    private val imageService: ImageService,
    private val privacyStatementDao: PrivacyStatementDao,
    @Value("\${medicinfo.admin.email:}") private val adminEmail: String?,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    lateinit var adminUser: User; private set

    @PostConstruct
    fun init() {
        require(!adminEmail.isNullOrEmpty()) { "Admin email not configured" }
        val user = dao.getByEmail(null, adminEmail)
        adminUser = if (user != null) {
            log.info("Existing admin user id is ${user.publicId}")
            user
        } else {
            val newUser = dao.createAdmin(AdminDto(displayName = "Admin", email = adminEmail))
            val apiKey = dao.generateApiKey(newUser.id)
            log.info("Created admin user ${newUser.publicId} with api key $apiKey")
            newUser
        }
    }

    fun getList(query: String? = null, order: List<String>? = null): List<UserDto> {
        return dao.getList(query, order).map {
            mapUser(it.asUser(), it.labelPublicId, PrivacyStatementDto(it.privacyVersion, it.privacyVersionAcceptedAt))
        }
    }

    fun getUser(id: UUID): UserDto {
        val user = dao.getByPublicId(id)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with id $id not found" }
        return mapUser(user, user.labelId?.let(labelDao::getById)?.publicId)
    }

    fun createAdmin(dto: AdminDto) = withApiKey(dao.createAdmin(dto))

    fun createEmployee(dto: EmployeeDto) = withApiKey(dao.createEmployee(dto))

    fun createCustomer(dto: CustomerDto): NewUserDto {
        val label = labelDao.getByPublicId(dto.labelId!!)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with id ${dto.labelId} not found" }
        val user = dao.createCustomer(label.id, dto)
        return withApiKey(user, label.publicId)
    }

    fun registerCustomer(dto: RegisterCustomerDto, authorizationHeader: String?): NewUserDto {
        val label = labelDao.getByPublicId(dto.labelId!!)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with id ${dto.labelId} not found" }
        validateRegistrationAuthorization(authorizationHeader, dto.labelId, dto.displayName!!)
        val user = dao.createCustomer(label.id, dto.toCustomerDto())
        dto.privacyVersion?.let { privacyStatementDao.getExistingOrCreate(user.id, it) }

        if (dto.customerOnboardingDetails != null) {
            val maybeLatestConversation = conversationDao.getLatest(user.id)
            dao.updateCustomersOnboardingDetails(
                user.id,
                dto.customerOnboardingDetails.customerEntryType?.salesforceTranslation,
                dto.customerOnboardingDetails.generalPractice,
                dto.customerOnboardingDetails.generalPracticeAGBcode,
                dto.customerOnboardingDetails.generalPracticeCenter,
                dto.customerOnboardingDetails.generalPracticeCenterAGBcode,
                dto.customerOnboardingDetails.holidayDestination,
                dto.customerOnboardingDetails.shelterLocationId,
                dto.customerOnboardingDetails.shelterLocationName,
            )
            salesforceService.sendOnboardingData(user, dto.customerOnboardingDetails, maybeLatestConversation?.publicId)
        } else {
            salesforceService.onCustomerUpdate(user)
        }

        if (!dto.deviceToken.isNullOrEmpty()) {
            failResponseIf(label.snsApplicationArn.isNullOrEmpty()) { "Label does not support push notifications" }
            deleteTokenIfNotUnique(label.id, dto.deviceToken)
            val arn =
                notificationService.createEndpoint(label.snsApplicationArn, dto.deviceToken, user.publicId.toString())
            dao.registerDeviceToken(user.id, dto.deviceToken, arn)
        }
        return withApiKey(user, label.publicId)
    }

    fun patchCustomer(dto: UpdateCustomerDto, user: User): CustomerDto {
        val updatedUser = dao.updateCustomer(
            user.id,
            dto.displayName ?: user.name,
            dto.email ?: user.email,
            dto.gender ?: user.gender,
            dto.age ?: user.age,
            dto.isInsured ?: user.isInsured,
            dto.birthdate?.let {
                zonedToDatabaseUtc(it)
            } ?: user.birthdate,
            dto.phoneNumber ?: user.phoneNumber,
            dto.postalCode ?: user.postalCode,
            dto.houseNumber ?: user.houseNumber,
        )
        dto.privacyVersion?.let { privacyStatementDao.getExistingOrCreate(user.id, it) }

        // If there is a customerEntryType, this function is called during onboarding of a user and all
        // customerOnboardingDetails should be sent to Salesforce
        // If customerOnboardingDetails is not null, but customerEntryType is null, then this is called in a patch
        // and the patched customerOnboardingDetails will be used in updateCustomer()
        if (dto.customerOnboardingDetails?.customerEntryType != null) {
            val maybeLatestConversation = conversationDao.getLatest(user.id)
            salesforceService.sendOnboardingData(
                updatedUser,
                dto.customerOnboardingDetails,
                maybeLatestConversation?.publicId
            )
        }

        return updateCustomer(updatedUser, dto.deviceToken ?: user.deviceToken, dto)
    }

    /**
     * This method calls salesforce to get birthdate of the user, only if user has null birthdate.
     * Due to iOS bug some users have null birthdate, it is used in the system for example
     * to check if triage question can be asked based on age of the user
     *
     * @param user User POJO
     * @throws IllegalStateException when salesforce responds with code different from 2xx or with empty body
     */
    fun syncUserBirthdateIfNotExist(user: User): User {
        if (user.birthdate == null) {
            val customerContactData = salesforceService.getCustomerContactData(user.publicId)
                ?: error("There is no contact data for user in salesforce")
            return customerContactData.birthdate?.let { dao.updateCustomersBirthdate(user.id, it.atTime(0, 0)) } ?: user
        }

        return user
    }

    /**
     * This method calls Salesforce to check if user is marked as inactive. If there is no data for the user, defaults to true
     *
     * @param user User POJO
     */
    fun canUserStartNewTriageInSalesforce(user: User): Boolean {
        return try {
            val customerContactData = salesforceService.getCustomerContactData(user.publicId)
            val isCustomerInactive = customerContactData?.inactive ?: false
            customerContactData?.let {
                updateUserWithSalesforceData(user, it)
            }
            !isCustomerInactive
        } catch (e: Exception) {
            log.error("Error has occured when getting customer data from Salesforce", e)
            // if salesforce connection is down, assume the customer is 'active' and continue triage
            // (instead of returning 500 to the user)
            true
        }
    }

    fun userNeedsToPassEHICCheck(user: User): Boolean {
        return try {
            val customerContactData = salesforceService.getCustomerContactData(user.publicId)
            customerContactData?.proposition == CustomerEntryType.HOLIDAY_FOREIGN_TOURIST.salesforceTranslation
        } catch (e: Exception) {
            log.error("Error has occured when getting customer data from Salesforce", e)
            false
        }
    }

    private fun updateUserWithSalesforceData(user: User, userContactData: UserContactDataSalesforceResponse) {
        if (!user.onboardingDetailsAdded) {
            try {
                dao.updateCustomersOnboardingDetails(
                    user.id,
                    userContactData.proposition,
                    userContactData.generalPractice,
                    userContactData.generalPracticeAGBcode,
                    userContactData.generalPracticeCenter,
                    userContactData.generalPracticeCenterAGBcode,
                    userContactData.holidayDestination,
                    userContactData.shelterLocationId,
                    userContactData.shelterLocationName,
                    )
            } catch (e: Exception) {
                log.error("Error has occured when getting customer data from Salesforce", e)
            }
        }
    }

    fun putCustomer(dto: CustomerDto, userId: UUID): CustomerDto {
        val user = dao.getByPublicId(userId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with given id not found" }
        val updatedUser = dao.updateCustomer(
            user.id,
            dto.displayName,
            dto.email,
            dto.gender,
            dto.age,
            dto.isInsured,
            dto.birthdate?.let {
                zonedToDatabaseUtc(it)
            },
            dto.phoneNumber,
            dto.postalCode,
            dto.houseNumber
        )

        dto.privacyVersion?.let { privacyStatementDao.getExistingOrCreate(user.id, it) }

        return updateCustomer(updatedUser, dto.deviceToken)
    }

    private fun updateCustomer(user: User, newDeviceToken: String?, dto: UpdateCustomerDto? = null): CustomerDto {
        val label = labelDao.getById(user.labelId)!!
        salesforceService.onCustomerUpdate(
            user,
            dto?.customerOnboardingDetails?.lastName
        )

        if (newDeviceToken != user.deviceToken) {
            failResponseIf(label.snsApplicationArn.isNullOrEmpty()) { "Label does not support push notifications" }

            if (user.snsEndpointArn != null) {
                notificationService.deleteEndpoint(user.snsEndpointArn)
            }
            if (!newDeviceToken.isNullOrEmpty()) {
                deleteTokenIfNotUnique(label.id, newDeviceToken)
                val arn = notificationService.createEndpoint(
                    label.snsApplicationArn,
                    newDeviceToken,
                    user.publicId.toString()
                )
                dao.registerDeviceToken(user.id, newDeviceToken, arn)
            }
        }
        return mapCustomer(dao.getById(user.id)!!, label.publicId)
    }

    fun deleteUser(id: UUID, actor: User) {
        val user = dao.getByPublicId(id)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with id $id not found" }
        failResponseIf(!isAdminOrUserIsActor(actor, user), HttpStatus.FORBIDDEN) { "Can only delete your own account" }

        deleteAttachmentsForUser(user)
        dao.delete(user.id)
    }

    fun deleteUserConversations(id: UUID, actor: User) {
        val user = dao.getByPublicId(id)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with id $id not found" }
        failResponseIf(
            !isAdminOrUserIsActor(actor, user),
            HttpStatus.FORBIDDEN
        ) { "Can only delete your own conversations" }

        deleteAttachmentsForUser(user)
        conversationDao.deleteUserConversations(user.id)
    }

    /**
     * This method deletes given user attachments (but leaves messages intact) by publicId(UUID).
     *
     * @param userId user publicId(UUID)
     * @param actor User object that will be the 'executor' of the deletion
     * @param attachmentIds list of ids to delete, if list is *null* then *all* attachments for the user will be deleted
     */
    fun deleteUserAttachments(userId: UUID, actor: User, attachmentIds: List<UUID>? = null) {
        val user = dao.getByPublicId(userId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with id $userId not found" }
        failResponseIf(
            !isAdminOrUserIsActor(actor, user),
            HttpStatus.FORBIDDEN
        ) { "Can only delete your own attachments" }

        deleteAttachmentsForUser(user, attachmentIds)
    }

    private fun isAdminOrUserIsActor(actor: User, user: User) = actor.role == UserRole.ADMIN || user.id == actor.id

    private fun deleteAttachmentsForUser(user: User, attachmentIds: List<UUID>? = null) {
        val attachments = when (attachmentIds) {
            null -> conversationDao.getAllUserAttachment(user.id)
            else -> conversationDao.getUserAttachments(user.id, attachmentIds)
        }
        attachments.forEach {
            imageService.deleteImage(it.id, it.s3Key)
        }
    }

    fun sendIdData(user: User, dto: IdDataDto) {
        val latestConversation =
            conversationDao.getLatest(user.id) ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) {
                "No conversation found for user"
            }

        try {
            salesforceService.sendIdDataAsync(user, dto, latestConversation.publicId)
        } catch (expectedEx: RuntimeException) {
            log.debug("Sending id data to salesforce failed", expectedEx)
            throw createResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE) {
                "Salesforce responded with an error: ${expectedEx.message}"
            }
        }
    }

    fun sendId(user: User, dto: IdDto) {
        val latestConversation =
            conversationDao.getLatest(user.id) ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) {
                "No conversation found for user"
            }

        try {
            salesforceService.sendIdAsync(user, dto, latestConversation.publicId)
        } catch (expectedEx: RuntimeException) {
            log.debug("Sending id data to salesforce failed", expectedEx)
            throw createResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE) {
                "Salesforce responded with an error: ${expectedEx.message}"
            }
        }
    }

    fun sendPushNotification(id: UUID, dto: PushNotificationDto) {
        val user = dao.getByPublicId(id)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with id $id not found" }
        failResponseIf(user.snsEndpointArn.isNullOrEmpty()) { "User $id does not have push notifications configured" }

        notificationService.sendCombinedNotification(
            user.snsEndpointArn,
            dto.title,
            dto.message,
            dto.data.orEmpty(),
            badge = dto.badge,
            sound = dto.sound ?: "default",
            platform = PushNotificationPlatform.GCM,
        )
    }

    private fun withApiKey(user: User, labelId: UUID? = null) =
        NewUserDto(dao.generateApiKey(user.id), mapUser(user, labelId))

    private fun mapUser(user: User, labelId: UUID?, privacyStatementDto: PrivacyStatementDto? = null) =
        when (user.role!!) {
            UserRole.ADMIN -> mapAdmin(user)
            UserRole.EMPLOYEE -> mapEmployee(user)
            UserRole.CUSTOMER -> mapCustomer(user, labelId!!, privacyStatementDto)
        }

    private fun mapAdmin(user: User) = AdminDto(user.publicId, databaseUtcToZoned(user.created), user.name, user.email)

    private fun mapEmployee(user: User) = EmployeeDto(user.publicId, databaseUtcToZoned(user.created), user.name)

    private fun mapCustomer(user: User, labelId: UUID, privacyStatementDto: PrivacyStatementDto? = null): CustomerDto {
        var latestPrivacyStatementEntry = privacyStatementDto
        if (privacyStatementDto == null) {
            val record = privacyStatementDao.findLatestByUser(user.id)
            latestPrivacyStatementEntry = PrivacyStatementDto(record?.version, record?.acceptedAt)
        }
        return CustomerDto(
            user.publicId,
            databaseUtcToZoned(user.created),
            user.name,
            labelId,
            user.email,
            user.gender,
            user.age,
            user.isInsured,
            privacyVersion = latestPrivacyStatementEntry?.version,
            privacyVersionAcceptedAt = latestPrivacyStatementEntry?.acceptedAt?.let { databaseUtcToZoned(it) },
            birthdate = user.birthdate?.let { databaseUtcToZoned(it) },
            deviceToken = user.deviceToken,
            phoneNumber = user.phoneNumber,
            postalCode = user.postalCode,
            houseNumber = user.houseNumber
        )
    }

    private fun validateRegistrationAuthorization(actual: String?, labelId: UUID, username: String) {
        if (actual.orEmpty().startsWith("Digest ", ignoreCase = true)) {
            val expectedAuthValue = registerCustomerAuthorization(labelId, username)
            if (actual!!.substring(7).equals(expectedAuthValue, ignoreCase = true)) return
        }
        throw createResponseStatusException(HttpStatus.UNAUTHORIZED) { "Invalid authorization" }
    }

    private fun deleteTokenIfNotUnique(labelId: Int, deviceToken: String) {
        val otherDeviceUsers = dao.getByDeviceToken(labelId, deviceToken)
        otherDeviceUsers.forEach { user ->
            notificationService.deleteEndpoint(user.snsEndpointArn)
            dao.unregisterDeviceToken(user.id)
        }
    }

    private fun UserView.asUser(): User =
        User(
            id,
            publicId,
            created,
            labelId,
            salesforceId,
            role,
            name,
            gender,
            age,
            email,
            isInsured,
            deviceToken,
            snsEndpointArn,
            null,
            null,
            birthdate,
            migratedFrom,
            phoneNumber,
            postalCode,
            houseNumber,
            entryType,
            generalPractice,
            generalPracticeAgbCode,
            generalPracticeCenter,
            generalPracticeCenterAgbCode,
            holidayDestination,
            onboardingDetailsAdded,
            shelterLocationId,
            shelterLocationName,
        )

    companion object {
        fun registerCustomerAuthorization(labelId: UUID, name: String): String? {
            return DigestUtils.sha256Hex("medicinfo-customer-registration;$labelId;$name")
        }
    }
}
