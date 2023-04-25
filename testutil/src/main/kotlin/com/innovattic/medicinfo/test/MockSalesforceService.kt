package com.innovattic.medicinfo.test

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
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAnswersRequestDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAttachmentData
import com.innovattic.medicinfo.logic.dto.salesforce.UserContactDataSalesforceResponse
import com.innovattic.medicinfo.logic.salesforce.SalesforceClient
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
class MockSalesforceService(
    private val salesforceClient: SalesforceClient
) : SalesforceService {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun onCustomerUpdate(user: User, lastname: String?) {
        log.debug("Update customer ${user.publicId}")
    }

    override fun onCustomerMessageAsync(
        label: Label,
        conversation: Conversation,
        message: Message,
        attachment: MessageAttachment?,
        imageBytes: ByteArray?,
        sender: User
    ) {
        log.debug("Message in ${conversation.publicId} by ${sender.role.value} ${sender.publicId}: ${message.message}")
    }

    override fun sendIdDataAsync(user: User, data: IdDataDto, conversationId: UUID) {
        log.debug("Send ID data for ${user.publicId}: $data")
    }

    override fun sendOnboardingData(user: User, data: CustomerOnboardingDetailsDto, conversationId: UUID?) {
        log.debug("Send Onboarding data for ${user.publicId}: $data")
    }

    override fun sendIdAsync(user: User, data: IdDto, conversationId: UUID) {
        log.debug("Send ID for ${user.publicId}: $data")
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
        log.debug(
            """
                Self test submitted by ${user.publicId}
                LabelId: ${label.id}
                complaintArea: $complaintArea
                degreeOfComplaints: $degreeOfComplaints
                questionsAndAnswers: $questionsAndAnswers
                result: $result
            """.trimIndent()
        )
    }

    // easy hack for getting a value that changes, but not too often, and doesn't return too high values
    // actual value doesn't matter, so this doesn't need to use the injected clock
    override fun getOnlineEmployeeCount() = LocalDateTime.now().minute

    override fun getGeneralPracticeCenters(contracted: Boolean): List<GeneralPracticeCenterDto> {
        log.debug("getGeneralPracticeCenters called")
        if (contracted) {
            return listOf(GeneralPracticeCenterDto("1", "general practice center with contract"))
        } else {
            return listOf(
                GeneralPracticeCenterDto("0", "general practice center without contract"),
                GeneralPracticeCenterDto("1", "general practice center with contract")
            )
        }
    }

    override fun getGeneralPractices(contracted: Boolean, labelCode: String?): List<GeneralPracticeDto> {
        log.debug("getGeneralPractices called with labelCode=$labelCode")
        return if (contracted) {
            listOf(GeneralPracticeDto("1", "general practice with contract"))
        } else {
            listOf(
                GeneralPracticeDto("0", "general practice without contract"),
                GeneralPracticeDto("1", "general practice with contract")
            )
        }
    }

    override fun getGeneralPracticePractitioners(generalPracticeCode: String): List<GeneralPracticePractitionerDto> {
        log.debug("getGeneralPracticePractitioners called with code: $generalPracticeCode")
        return listOf(GeneralPracticePractitionerDto("0", "general practice practitioner"))
    }

    override fun getHolidayDestinations(): List<HolidayDestinationDto> {
        log.debug("getHolidayDestinations called")
        return listOf(HolidayDestinationDto("0", "holiday destination"))
    }

    override fun getShelterLocations(): List<ShelterLocationDto> {
        log.debug("getShelterLocations called")
        return listOf(
            ShelterLocationDto("0", "Shelter location number 1"),
            ShelterLocationDto("1", "Shelter location number 2")
        )
    }

    override fun closeSelfTestAsync(user: User, chatId: UUID?) {
        log.debug(
            "CloseSelfTest test submitted by ${user.publicId} with chatId: $chatId"
        )
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
        try {
            salesforceClient.post("meeting-arrrange-test-Sm52iAfkeo", "", String::class)
            onSuccess.invoke(
                SalesforceAppointment(
                    "appointment-id-${UUID.randomUUID()}",
                    salesforceEventId ?: "appointment-event-id",
                    "https://video-url.com"
                )
            )
        } catch (ex: Exception) {
            onFail.invoke(ex)
        }
        log.debug(
            """
                ArrangeAppointment submitted by ${user.publicId} with
                AppointmentId: $salesforceAppointmentId
                EventId: $salesforceEventId
                CoachName: $coachName
                CoachEmail: $coachEmail
                LabelCode: $labelCode
                StartDateTime: $startDateTime
                EndDateTime: $endDateTime
                Subject: $subject
                IsRescheduled: $isRescheduled
                IsIntakeAppointment: $isIntakeAppointment
            """.trimIndent()
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
            """
                CancelAppointment submitted by $customerId with
                AppointmentId: $appointmentId
                EventId: $eventId
                Reason: $cancelReason
                isCancelledByClient: $isCancelledByClient
            """.trimIndent()
        )
    }

    override fun sendTriageAnswers(body: SalesforceTriageAnswersRequestDto) {
        log.debug("Send triage answers to mock salesforce service: $body")
    }

    override fun addImagesToTriageAsync(
        userDataWithImages: SalesforceTriageAttachmentData,
        getDownloadForId: (String) -> Download
    ) {
        log.debug("Send triage images to mock salesforce service: $userDataWithImages")
    }

    override fun addEhicImageToTriage(
        conversationId: UUID,
        userId: UUID,
        filename: String,
        contentType: String,
        inputStream: InputStream
    ): AddAttachmentToCaseResponse {
        log.debug("Send EHIC image to mock salesforce service $filename")
        return AddAttachmentToCaseResponse(true)
    }

    override fun getCustomerContactData(userId: UUID): UserContactDataSalesforceResponse? {
        log.debug("Update customer birthdate for userUUID: $userId")

        return UserContactDataSalesforceResponse(LocalDate.of(2000, 3, 1), false, userId)
    }
}
