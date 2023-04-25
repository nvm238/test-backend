package com.innovattic.medicinfo.logic.salesforce

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.Suppliers
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.innovattic.common.database.zonedToDatabaseUtc
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dto.CustomerOnboardingDetailsDto
import com.innovattic.medicinfo.database.dto.GeneralPracticeCenterDto
import com.innovattic.medicinfo.database.dto.GeneralPracticeDto
import com.innovattic.medicinfo.database.dto.GeneralPracticePractitionerDto
import com.innovattic.medicinfo.database.dto.HolidayDestinationDto
import com.innovattic.medicinfo.database.dto.ShelterLocationDto
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.Label
import com.innovattic.medicinfo.dbschema.tables.pojos.Message
import com.innovattic.medicinfo.dbschema.tables.pojos.MessageAttachment
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.Download
import com.innovattic.medicinfo.logic.dto.IdDataDto
import com.innovattic.medicinfo.logic.dto.IdDto
import com.innovattic.medicinfo.logic.dto.salesforce.AddAttachmentToCaseResponse
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceAppointment
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceGeneralPracticeCentersDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceGeneralPracticePractitionersDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceGeneralPracticesDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceHolidayDestinationsDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceShelterLocationListDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAnswersRequestDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAttachmentData
import com.innovattic.medicinfo.logic.dto.salesforce.UserContactDataSalesforceResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy

class RealSalesforceService(
    private val salesforceClient: SalesforceClient
) : SalesforceService {
    companion object {
        const val POOL_SIZE = 1
        const val QUEUE_SIZE = 100000
    }

    private val log = LoggerFactory.getLogger(javaClass)

    // Create a custom pool so that we can queue calls to salesforce per conversationId, also called chatId,
    // this ensures that multiple calls can happen concurrently but all calls belonging to the same conversationId are
    // send sequentially. This ensures quick and a correct arrival at Salesforce.
    private val pool = PerConversationThreadPool()

    val objectMapper = run {
        val jacksonMapper = jacksonObjectMapper()
        jacksonMapper.registerModule(JavaTimeModule())
        jacksonMapper.propertyNamingStrategy = PropertyNamingStrategies.SnakeCaseStrategy()
        jacksonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        jacksonMapper
    }

    private val onlineEmployees = Suppliers.memoizeWithExpiration({
        salesforceClient.get("get-online-employees", Map::class).body!!["NumberOfEmployees"] as Int
    }, 1, TimeUnit.MINUTES)

    private val generalPracticeCenters = Suppliers.memoizeWithExpiration({
        val isSelection = false // required, true for our use case of retrieving centers with contract
        val body = salesforceClient.get("get-general-practice-centers/$isSelection", String::class).body
        objectMapper.readValue(body, SalesforceGeneralPracticeCentersDto::class.java)
    }, 8, TimeUnit.HOURS)

    private val generalPracticeCentersWithContract = Suppliers.memoizeWithExpiration({
        val isSelection = true // required, true for our use case of retrieving centers with contract
        val body = salesforceClient.get("get-general-practice-centers/$isSelection", String::class).body
        objectMapper.readValue(body, SalesforceGeneralPracticeCentersDto::class.java)
    }, 8, TimeUnit.HOURS)

    private val holidayDestinations = Suppliers.memoizeWithExpiration({
        val body = salesforceClient.get("get-holiday-destinations", String::class).body
        objectMapper.readValue(body, SalesforceHolidayDestinationsDto::class.java)
    }, 8, TimeUnit.HOURS)

    private val shelterLocations = Suppliers.memoizeWithExpiration({
        val body = salesforceClient.get("get-shelter-locations", String::class).body
        objectMapper.readValue(body, SalesforceShelterLocationListDto::class.java)
    }, 8, TimeUnit.HOURS)

    private data class GetGeneralPracticesParams(
        val isSelection: Boolean,
        val labelCode: String?
    )

    private fun getGeneralPracticesUncached(params: GetGeneralPracticesParams): SalesforceGeneralPracticesDto? {
        val (isSelection, labelCode) = params
        val body = salesforceClient.get("get-general-practices/$isSelection/$labelCode", String::class).body
        return objectMapper.readValue(body, SalesforceGeneralPracticesDto::class.java)
    }

    private val generalPracticesCache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.of(8, ChronoUnit.HOURS))
        .build(CacheLoader.from(::getGeneralPracticesUncached))

    private fun getContactDataUncached(userId: UUID): UserContactDataSalesforceResponse? {
        return salesforceClient.get("get-contactdata/$userId", UserContactDataSalesforceResponse::class).body
    }

    private val userContactDataCache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.of(1, ChronoUnit.HOURS))
        .build(CacheLoader.from(::getContactDataUncached))

    init {
        pool.start()
    }

    @PreDestroy
    fun cleanup() {
        pool.stop()
    }

    override fun getOnlineEmployeeCount(): Int = onlineEmployees.get()

    override fun getGeneralPracticeCenters(contracted: Boolean): List<GeneralPracticeCenterDto> {
        val list = if (contracted) generalPracticeCentersWithContract.get().list else generalPracticeCenters.get().list
        return list.map { GeneralPracticeCenterDto(it.code, it.name) }
    }

    override fun getGeneralPractices(contracted: Boolean, labelCode: String?): List<GeneralPracticeDto> {
        val dto = generalPracticesCache[GetGeneralPracticesParams(contracted, labelCode)]
            ?: return emptyList()

        return dto.list.map { GeneralPracticeDto(it.code, it.name) }
    }

    override fun getGeneralPracticePractitioners(generalPracticeCode: String): List<GeneralPracticePractitionerDto> {
        val body = salesforceClient.get("get-general-practitioners/$generalPracticeCode", String::class).body
        val list = objectMapper.readValue(body, SalesforceGeneralPracticePractitionersDto::class.java).list

        return list.map { GeneralPracticePractitionerDto(it.code, it.name) }
    }

    override fun getHolidayDestinations(): List<HolidayDestinationDto> {
        val list = holidayDestinations.get().list
        return list.map { HolidayDestinationDto(it.id, it.name) }
    }

    override fun getShelterLocations(): List<ShelterLocationDto> {
         val list = shelterLocations.get().list
        return list.map { ShelterLocationDto(it.id, it.name) }
    }

    override fun closeSelfTestAsync(user: User, chatId: UUID?) {
        val body = mapOf(
            "customerId" to user.publicId,
            "chatId" to chatId,
            "customerName" to user.name
        )
        if (chatId != null) {
            pool.addEvent(Runnable { salesforceClient.post("close-selftest", body, Any::class) }, chatId)
        } else {
            pool.addNonConversationEvent { salesforceClient.post("close-selftest", body, Any::class) }
        }
    }

    override fun onCustomerUpdate(user: User, lastname: String?) {
        // checked with Wim and Jasper: Putting full display name in last name field is OK when lastname is null
        val (salesforceFirstName, salesforceLastName) = if (lastname == null) {
            "" to user.name
        } else {
            user.name to lastname
        }
        val body = mapOf(
            "customerId" to user.publicId,
            "firstname" to salesforceFirstName,
            "gender" to user.gender?.value,
            "lastname" to salesforceLastName,
            "isInsured" to user.isInsured,
            "phoneNumber" to user.phoneNumber,
            "zipcode" to user.postalCode,
            "houseNumber" to user.houseNumber,
            "email" to user.email,
        )

        try {
            salesforceClient.post("add-profile", body, Any::class)
        } catch (ignoreException: RuntimeException) {
            throw createResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR) { "Failed to post customer details." }
        }
    }

    override fun onCustomerMessageAsync(
        label: Label,
        conversation: Conversation,
        message: Message,
        attachment: MessageAttachment?,
        imageBytes: ByteArray?,
        sender: User
    ) {
        val messageBody = mapOf(
            "chatId" to conversation.publicId,
            "created" to message.created.toString(),
            "customerId" to sender.publicId,
            "customerName" to sender.name,
            "id" to message.publicId,
            "labelCode" to label.code,
            "message" to message.message,
            "language" to conversation.language
        )

        pool.addEvent(
            Runnable {
                salesforceClient.post("process-chat-message", messageBody, Any::class)
                if (attachment != null && imageBytes != null) {
                    postAttachmentToCase(
                        conversation.publicId,
                        sender.publicId,
                        attachment.attachmentType.name,
                        attachment.publicId.toString()
                    ) { Download(attachment.contentType, ByteArrayInputStream(imageBytes)) }
                }
            },
            conversation.publicId
        )
        log.info("Submit new Salesforce call on pool. Pool size={}", pool.queueSize)
    }

    override fun sendIdDataAsync(user: User, data: IdDataDto, conversationId: UUID) {
        val body = mapOf(
            "birthdate" to data.birthDate?.let { zonedToDatabaseUtc(it).toString() },
            "bsn" to data.bsn,
            "chatId" to conversationId,
            "customerId" to user.publicId,
            "firstname" to data.firstName,
            "gender" to user.gender.value,
            "idNumber" to data.idNumber,
            "idType" to data.idType!!.salesforceValue,
            "lastname" to data.lastName,
        )
        pool.addEvent(Runnable { salesforceClient.post("receive-id-data", body, Any::class) }, conversationId)
    }

    override fun sendOnboardingData(user: User, data: CustomerOnboardingDetailsDto, conversationId: UUID?) {
        val body = mapOf(
            "birthdate" to toSalesforceDate(user.birthdate),
            "bsn" to data.bsn,
            "chatId" to (conversationId ?: ""),
            "customerId" to user.publicId,
            "firstname" to user.name,
            "gender" to user.gender.value,
            // checked with Wim and Jasper: Putting full display name in last name field is OK
            "lastname" to data.lastName,
            "generalPractice" to data.generalPractice,
            "generalPracticeAGBcode" to data.generalPracticeAGBcode,
            "generalPracticeCenter" to data.generalPracticeCenter,
            "generalPracticeCenterAGBcode" to data.generalPracticeCenterAGBcode,
            "generalPractitioner" to data.generalPracticePractitioner,
            "generalPractitionerAGBcode" to data.generalPracticePractitionerAGBcode,
            "holidayDestination" to data.holidayDestination,
            "shelterLocationId" to data.shelterLocationId,
            "proposition" to data.customerEntryType?.salesforceTranslation,
            "phoneNumber" to user.phoneNumber,
            "zipcode" to user.postalCode,
            "houseNumber" to user.houseNumber
        )

        val result = try {
            val response = salesforceClient.post("add-onboarding-data", body, Map::class).body
            response["covStatus"] as String
        } catch (ignoreException: RuntimeException) {
            throw createResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR) {
                "Failed to post onboarding details."
            }
        }

        when (result) {
            "COVcheckCorrect", "COVcheckNotNeeded" -> return
            "COVcheckIncorrect" -> throw createResponseStatusException(
                HttpStatus.FORBIDDEN,
                code = ErrorCodes.COV_INCORRECT
            ) { "COV Check incorrect" }

            else -> throw createResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR) { "Unknown COV Check response received" }
        }
    }

    private fun toSalesforceDate(birthdate: LocalDateTime?): String? {
        return birthdate?.let { DateTimeFormatter.ofPattern("yyyy-MM-dd").format(it) }
    }

    override fun sendIdAsync(user: User, data: IdDto, conversationId: UUID) {
        val body = mapOf(
            "customerId" to user.publicId,
            "idNumber" to data.idNumber,
            "idType" to data.idType!!.salesforceValue,
            "chatId" to conversationId
        )
        pool.addEvent(Runnable { salesforceClient.post("add-id-data", body, Any::class) }, conversationId)
    }

    override fun submitSelfTestAsync(
        label: Label,
        user: User,
        complaintArea: String?,
        degreeOfComplaints: String?,
        personalAssistance: String,
        questionsAndAnswers: String,
        result: String,
        conversationId: UUID?
    ) {
        val body = mapOf(
            "complaintArea" to complaintArea,
            "customerId" to user.publicId,
            "degreeOfComplaints" to degreeOfComplaints,
            "labelCode" to label.code,
            "personalAssistance" to personalAssistance,
            "questionnaireResult" to questionsAndAnswers,
            "resultWithComplaint" to result,
            "isInsured" to user.isInsured,
            "customerName" to user.name,
            "chatId" to conversationId,
        )
        if (conversationId != null) {
            pool.addEvent(Runnable { salesforceClient.post("add-selftest", body, Any::class) }, conversationId)
        } else {
            pool.addNonConversationEvent {
                salesforceClient.post("add-selftest", body, Any::class)
            }
        }
    }

    override fun arrangeAppointmentAsync(
        user: User,
        salesforceAppointmentId: String?,
        salesforceEventId: String?,
        coachName: String,
        coachEmail: String,
        labelCode: String,
        startDateTime: Instant,
        endDateTime: Instant,
        subject: String,
        isRescheduled: Boolean,
        isIntakeAppointment: Boolean,
        onSuccess: (SalesforceAppointment) -> Unit,
        onFail: (Exception) -> Unit
    ) {
        val body = mapOf(
            "customerId" to user.publicId,
            "appointmentId" to salesforceAppointmentId,
            "eventId" to salesforceEventId,
            "coachName" to coachName,
            "coachEmail" to coachEmail,
            "labelCode" to labelCode,
            "endDateTime" to DateTimeFormatter.ISO_INSTANT.format(endDateTime),
            "startDateTime" to DateTimeFormatter.ISO_INSTANT.format(startDateTime),
            "isCancelled" to false,
            "isRescheduled" to isRescheduled,
            "participants" to arrayOf(
                mapOf(
                    "email" to user.email,
                    "name" to user.name
                )
            ),
            "subject" to subject,
            "isIntake" to isIntakeAppointment
        )
        pool.addNonConversationEvent(
            Runnable {
                try {
                    val response = salesforceClient.post("arrange-meeting", body, Map::class).body
                    val salesForceAppointmentId = response["appointmentId"] as String
                    val salesForceEventId = response["eventId"] as String
                    val videoMeetingUrl = response["meetingUrl"] as String
                    onSuccess.invoke(SalesforceAppointment(salesForceAppointmentId, salesForceEventId, videoMeetingUrl))
                } catch (e: Exception) {
                    onFail.invoke(e)
                }
            }
        )
    }

    override fun cancelAppointmentAsync(
        customerId: UUID,
        appointmentId: String,
        eventId: String,
        isCancelledByClient: Boolean,
        cancelReason: String
    ) {
        log.debug(
            "Sending cancel appointment for customer with id $customerId and appointment " +
                    "with id $appointmentId and event with id $eventId"
        )

        val body = mapOf(
            "appointmentId" to appointmentId,
            "customerId" to customerId,
            "eventId" to eventId,
            "isCancelled" to true,
            "isRescheduled" to false,
            "closedReason" to cancelReason,
            "isCancelledByClient" to isCancelledByClient,
        )
        pool.addNonConversationEvent { salesforceClient.post("arrange-meeting", body, Any::class) }
    }

    override fun sendTriageAnswers(body: SalesforceTriageAnswersRequestDto) {
        try {
            salesforceClient.post("add-triage-answers", body, Any::class)
        } catch (ignoreException: RuntimeException) {
            throw createResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR) {
                "Failed to post triage answers."
            }
        }
    }

    override fun addImagesToTriageAsync(
        userDataWithImages: SalesforceTriageAttachmentData,
        getDownloadForId: (String) -> Download
    ) {
        pool.addEvent(
            Runnable {
                userDataWithImages.attachments.forEach { questionWithImages ->
                    questionWithImages.imageIds.forEachIndexed { idx, imageId ->
                        postAttachmentToCase(
                            userDataWithImages.conversationId,
                            userDataWithImages.userId,
                            questionWithImages.questionId,
                            "${questionWithImages.questionId}_$idx"
                        ) { getDownloadForId(imageId) }
                    }
                }
            },
            userDataWithImages.conversationId
        )
    }

    override fun addEhicImageToTriage(
        conversationId: UUID,
        userId: UUID,
        filename: String,
        contentType: String,
        inputStream: InputStream
    ): AddAttachmentToCaseResponse? {
        val response = postAttachmentToCase(conversationId, userId, "", filename, isEhic = true) { Download(contentType, inputStream) }
        log.info("EHIC image attachment with filename: $filename successfully uploaded.")
        return response
    }

    private fun postAttachmentToCase(
        conversationId: UUID,
        userId: UUID,
        description: String,
        filename: String,
        isEhic: Boolean = false,
        getContentTypeAndStream: () -> Download,
    ): AddAttachmentToCaseResponse? {
        val (contentType, stream) = getContentTypeAndStream.invoke()
        val attachmentBody = stream.use {
            val contentBase64 = Base64.getEncoder().encodeToString(it.readAllBytes())
            mapOf(
                "chatId" to conversationId.toString(),
                "customerId" to userId.toString(),
                "contentBase64" to contentBase64,
                "contentType" to contentType,
                "description" to description,
                "fileName" to filename,
                "isEHIC" to isEhic,
            )
        }

        log.info(
            "Attachment upload start! ConversationId={}, userId={}, filename={}, description={}",
            conversationId.toString(), userId.toString(), filename, description
        )
        val response = salesforceClient.post("add-attachment-to-case", attachmentBody, AddAttachmentToCaseResponse::class).body
        log.info("Attachment upload with filename={} completed", filename)
        return response
    }

    override fun getCustomerContactData(userId: UUID): UserContactDataSalesforceResponse? {
        log.info("Get customer contact data from salesforce for userId=$userId")

        return userContactDataCache[userId]
    }
}
