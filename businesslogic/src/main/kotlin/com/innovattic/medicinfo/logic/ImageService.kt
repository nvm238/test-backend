package com.innovattic.medicinfo.logic

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.error.failResponseIf
import com.innovattic.common.file.FileService
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dao.MessageAttachmentDao
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.AttachmentType
import com.innovattic.medicinfo.dbschema.tables.pojos.MessageAttachment
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import org.springframework.http.HttpStatus
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.io.InputStream
import java.time.LocalDateTime
import java.util.UUID

@Component
class ImageService(
    private val fileService: FileService,
    private val dao: MessageAttachmentDao,
    private val labelDao: LabelDao,
    private val userDao: UserDao,
    private val conversationService: ConversationService
) {

    /**
     * This method saves the entry in database, generates imageKey and stores the file on file system
     *
     * @param name not used for now as we have no column in database to store filename
     * @param type content type ex. image/png
     * @param imageBytes ByteArray instance containing image
     * @param labelId integer ID of the label
     * @param userId UUID(publicId) of the user
     * @param messageId integer ID of the message that has the image included
     */
    @Suppress("UnusedPrivateMember")
    fun createImage(
        name: String,
        type: String,
        imageBytes: ByteArray,
        labelId: Int,
        userId: UUID,
        messageId: Int
    ): MessageAttachment {
        val user =
            userDao.getByPublicId(userId)
                ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with given id not found" }

        // empty s3Key is passed here as it is composed of label code and attachment's publicId(UUID)
        // which does not exist before it is inserted into the database. After insertion, UUID is
        // generated, image key is assembled and entry is updated with right key
        var attachment = dao.create("", type, messageId, user.id, AttachmentType.IMAGE)
        val labelCode = labelDao.getById(labelId)?.code!!
        val imageKey = imageKey(attachment.publicId, labelCode)
        attachment = dao.updateS3Key(attachment.id, imageKey)
        fileService.writeFile(imageKey, type, imageBytes)

        return attachment
    }

    fun checkTimeConstraint(userId: UUID) {
        val now = LocalDateTime.now()
        val timeLimit = now.minusMinutes(UPLOAD_TIME_LIMIT_MINUTES)
        val user = userDao.getByPublicId(userId) ?: return
        val count = dao.getNumberOfImagesWithTimeLimit(user.id, timeLimit)
        failResponseIf(count > FILE_NUMBER_LIMIT, code = ErrorCodes.UPLOAD_LIMIT_REACHED) {
            "Can't upload more than $FILE_NUMBER_LIMIT, in less than $UPLOAD_TIME_LIMIT_MINUTES"
        }
    }

    fun validateContentType(type: String): String {
        val valid = IMAGE_MEDIA_TYPE.any {
            MediaType.parseMediaType(type).isCompatibleWith(it)
        }
        try {
            failResponseIf(!valid, code = ErrorCodes.CONTENT_TYPE) {
                "Unsupported content type $type, must be an image"
            }
            return type
        } catch (ignoreException: InvalidMediaTypeException) {
            throw createResponseStatusException { "Invalid content type $type" }
        }
    }

    fun getImage(conversationId: UUID, imageId: UUID, user: User): Triple<UUID, MediaType, InputStream> {
        val conversation = conversationService.get(conversationId, user)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Conversation with given id not found" }
        val labelCode = labelDao.getById(conversation.labelId)?.code
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with given code not found" }
        val messageAttachment = dao.getById(imageId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Image with given id not found" }
        val mediaType = MediaType.parseMediaType(messageAttachment.contentType)

        return Triple(messageAttachment.publicId, mediaType, fileService.readFile(imageKey(imageId, labelCode)))
    }

    fun deleteImage(attachmentId: Int, fileKey: String) {
        fileService.deleteFile(fileKey)
        dao.deleteById(attachmentId)
    }

    companion object {
        val IMAGE_MEDIA_TYPE = listOf(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG, MediaType("image", "jpg"))
        const val UPLOAD_TIME_LIMIT_MINUTES = 10L
        const val FILE_NUMBER_LIMIT = 5

        fun imageKey(imageId: UUID, labelFolder: String) = "$labelFolder/$imageId"
    }
}
