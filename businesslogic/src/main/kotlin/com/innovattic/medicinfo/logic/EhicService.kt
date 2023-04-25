package com.innovattic.medicinfo.logic

import com.innovattic.common.error.failResponseIf
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dao.TriageStatusDao
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.EhicUploadResponse
import com.innovattic.medicinfo.logic.dto.UpdateCustomerDto
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class EhicService(
    private val salesforceService: SalesforceService,
    private val userService: UserService,
    private val conversationDao: ConversationDao,
    private val triageStatusDao: TriageStatusDao,
) {
    companion object {
        const val EHIC_FILE_NAME = "ehic-image"
    }
    fun uploadEhicImage(user: User, contentType: String, input: InputStream): EhicUploadResponse {
        val (conversation, triageStatus) = getConversationAndTriageStatus(user)
        val addAttachmentToCaseResponse = salesforceService.addEhicImageToTriage(
            conversation.publicId,
            user.publicId, EHIC_FILE_NAME,
            contentType,
            input
        )
        if (addAttachmentToCaseResponse?.isValidatedEHIC == true) {
            triageStatusDao.startTriageStatus(triageStatus.id)
        }
        return EhicUploadResponse(addAttachmentToCaseResponse?.isValidatedEHIC ?: false)
    }

    fun addEmail(user: User, email: String) {
        val (_, triageStatus) = getConversationAndTriageStatus(user)
        userService.patchCustomer(UpdateCustomerDto(email = email), user)
        triageStatusDao.startTriageStatus(triageStatus.id)
    }

    private fun getConversationAndTriageStatus(user: User): Pair<Conversation, TriageStatus> {
        val conversation = conversationDao.getLatestOpen(user.id)
        failResponseIf(conversation == null, HttpStatus.NOT_FOUND, ErrorCodes.CONVERSATION_MISSING) {
            "No open conversation found for user with id: ${user.publicId}"
        }
        val triageStatus = triageStatusDao.getByConversationId(conversation.id)
        failResponseIf(triageStatus == null, HttpStatus.NOT_FOUND) {
            "No triage found for conversation with conversation id: ${conversation.id}"
        }

        return Pair(conversation, triageStatus)
    }
}
