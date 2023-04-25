package com.innovattic.medicinfo.logic.salesforce

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
import java.io.InputStream
import java.time.Instant
import java.util.*

interface SalesforceService {
    fun onCustomerUpdate(user: User, lastname: String? = null)
    fun onCustomerMessageAsync(
        label: Label,
        conversation: Conversation,
        message: Message,
        attachment: MessageAttachment?,
        imageBytes: ByteArray?,
        sender: User
    )

    fun sendIdDataAsync(user: User, data: IdDataDto, conversationId: UUID)
    fun sendOnboardingData(user: User, data: CustomerOnboardingDetailsDto, conversationId: UUID?)
    fun sendIdAsync(user: User, data: IdDto, conversationId: UUID)
    fun submitSelfTestAsync(
        label: Label,
        user: User,
        complaintArea: String?,
        degreeOfComplaints: String?,
        personalAssistance: String,
        questionsAndAnswers: String,
        result: String,
        conversationId: UUID?
    )

    fun getOnlineEmployeeCount(): Int

    fun getGeneralPracticeCenters(contracted: Boolean): List<GeneralPracticeCenterDto>

    /**
     * Get list of general practices
     *
     * @param contracted true for retrieving all practices, false for retrieving practices with contract
     * @param labelCode nullable label code to restrict the returned general practices, if null then it will return all available
     */
    fun getGeneralPractices(contracted: Boolean, labelCode: String?): List<GeneralPracticeDto>

    fun getGeneralPracticePractitioners(generalPracticeCode: String): List<GeneralPracticePractitionerDto>

    fun getHolidayDestinations(): List<HolidayDestinationDto>

    fun getShelterLocations(): List<ShelterLocationDto>

    fun getCustomerContactData(userId: UUID): UserContactDataSalesforceResponse?

    fun closeSelfTestAsync(
        user: User,
        chatId: UUID?,
    )

    fun arrangeAppointmentAsync(
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
    )

    fun cancelAppointmentAsync(
        customerId: UUID,
        appointmentId: String,
        eventId: String,
        isCancelledByClient: Boolean,
        cancelReason: String
    )

    fun sendTriageAnswers(body: SalesforceTriageAnswersRequestDto)

    fun addImagesToTriageAsync(userDataWithImages: SalesforceTriageAttachmentData, getDownloadForId: (String) -> Download)

    fun addEhicImageToTriage(
        conversationId: UUID,
        userId: UUID,
        filename: String,
        contentType: String,
        inputStream: InputStream
    ): AddAttachmentToCaseResponse?
}
