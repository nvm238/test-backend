package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.medicinfo.database.dto.AttachmentType
import com.innovattic.medicinfo.dbschema.Tables.MESSAGE_ATTACHMENT
import com.innovattic.medicinfo.dbschema.tables.pojos.MessageAttachment
import org.jetbrains.annotations.TestOnly
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Component
class MessageAttachmentDao(private val context: DSLContext, private val clock: Clock) {

    fun create(
        s3Key: String,
        type: String,
        messageId: Int,
        userId: Int,
        attachmentType: AttachmentType
    ): MessageAttachment {
        return context.insertRecord(MESSAGE_ATTACHMENT) {
            it.messageId = messageId
            it.customerId = userId
            it.created = databaseNow(clock)
            it.s3Key = s3Key
            it.contentType = type
            it.attachmentType = attachmentType
        }.returningPojo()
    }

    fun updateS3Key(attachmentId: Int, s3Key: String): MessageAttachment =
        context.updateRecord(MESSAGE_ATTACHMENT) {
            it.s3Key = s3Key
        }.where(MESSAGE_ATTACHMENT.ID.eq(attachmentId))
            .returningPojo()

    fun getById(imageId: UUID): MessageAttachment? {
        return context.selectFrom(MESSAGE_ATTACHMENT).where(MESSAGE_ATTACHMENT.PUBLIC_ID.eq(imageId)).fetchOnePojo()
    }

    fun deleteById(imageId: Int) {
        context.deleteFrom(MESSAGE_ATTACHMENT)
            .where(MESSAGE_ATTACHMENT.ID.eq(imageId))
            .execute()
    }

    fun getNumberOfImagesWithTimeLimit(userId: Int, timeStamp: LocalDateTime): Int {
        return context.fetchCount(
            MESSAGE_ATTACHMENT,
            MESSAGE_ATTACHMENT.CUSTOMER_ID.eq(userId).and(MESSAGE_ATTACHMENT.CREATED.gt(timeStamp))
        )
    }

    @TestOnly
    fun clear(): Int {
        return context.deleteFrom(MESSAGE_ATTACHMENT).execute()
    }
}
