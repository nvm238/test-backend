package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.dbschema.Tables.CONVERSATION
import com.innovattic.medicinfo.dbschema.Tables.MESSAGE_ATTACHMENT
import com.innovattic.medicinfo.dbschema.Tables.MESSAGE_VIEW
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.MessageAttachment
import org.apache.commons.lang3.LocaleUtils
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.*

@Component
class ConversationDao(private val context: DSLContext, private val clock: Clock) {

    fun create(customerId: Int, labelId: Int, status: ConversationStatus, locale: Locale = LocaleUtils.toLocale("nl")!!): Conversation {
        return context.insertRecord(CONVERSATION) {
            it.created = databaseNow(clock)
            it.customerId = customerId
            it.labelId = labelId
            it.status = status
            it.language = locale.language
        }.returningPojo()
    }

    fun getLatest(customerId: Int): Conversation? {
        return context
            .selectFrom(CONVERSATION)
            .where(CONVERSATION.CUSTOMER_ID.eq(customerId))
            .orderBy(CONVERSATION.CREATED.desc())
            .limit(1)
            .fetchOnePojo()
    }

    fun getLatestOpen(customerId: Int): Conversation? {
        return context
            .selectFrom(CONVERSATION)
            .where(CONVERSATION.CUSTOMER_ID.eq(customerId).and(CONVERSATION.STATUS.eq(ConversationStatus.OPEN)))
            .orderBy(CONVERSATION.CREATED.desc())
            .limit(1)
            .fetchOnePojo()
    }

    fun getAllUserAttachment(customerId: Int): List<MessageAttachment> {
        return createQueryForAllUserAttachments(customerId)
            .fetchInto(MessageAttachment::class.java)
    }

    fun getUserAttachments(customerId: Int, attachmentIds: List<UUID>): List<MessageAttachment> {
        return createQueryForAllUserAttachments(customerId)
            .where(MESSAGE_ATTACHMENT.PUBLIC_ID.`in`(attachmentIds))
            .fetchInto(MessageAttachment::class.java)
    }

    private fun createQueryForAllUserAttachments(customerId: Int) = context
        .select(MESSAGE_ATTACHMENT.fields().asList())
        .from(MESSAGE_ATTACHMENT)
        .join(CONVERSATION).on(CONVERSATION.CUSTOMER_ID.eq(customerId))
        .join(MESSAGE_VIEW).on(MESSAGE_VIEW.CONVERSATION_ID.eq(CONVERSATION.ID))
        .and(MESSAGE_ATTACHMENT.PUBLIC_ID.eq(MESSAGE_VIEW.ATTACHMENT_ID))

    fun get(conversationPublicId: UUID): Conversation? {
        return context.fetchOnePojo(
            CONVERSATION,
            CONVERSATION.PUBLIC_ID.eq(conversationPublicId)
        )
    }

    fun get(conversationId: Int): Conversation? {
        return context.fetchOnePojo(
            CONVERSATION,
            CONVERSATION.ID.eq(conversationId)
        )
    }

    fun get(
        conversationPublicId: UUID,
        customerId: Int,
        labelId: Int
    ): Conversation? {
        return context.fetchOnePojo(
            CONVERSATION,
            CONVERSATION.PUBLIC_ID.eq(conversationPublicId)
                .and(CONVERSATION.LABEL_ID.eq(labelId))
                .and(CONVERSATION.CUSTOMER_ID.eq(customerId))
        )
    }

    fun received(conversationId: Int, customer: Boolean): Int {
        return context.updateRecord(CONVERSATION) {
            if (customer) {
                it.deliveredToCustomer = databaseNow(clock)
            } else {
                it.deliveredToEmployee = databaseNow(clock)
            }
        }.where(
            CONVERSATION.ID.eq(conversationId)
        ).execute()
    }

    fun read(conversationId: Int, customer: Boolean): Int {
        return context.updateRecord(CONVERSATION) {
            if (customer) {
                it.readByCustomer = databaseNow(clock)
            } else {
                it.readByEmployee = databaseNow(clock)
            }
        }.where(
            CONVERSATION.ID.eq(conversationId)
        ).execute()
    }

    fun archive(conversationPublicId: UUID, customerId: Int, labelId: Int): Int {
        return context.updateRecord(CONVERSATION) {
            it.status = ConversationStatus.ARCHIVED
        }.where(
            CONVERSATION.PUBLIC_ID.eq(conversationPublicId)
                .and(CONVERSATION.CUSTOMER_ID.eq(customerId))
                .and(CONVERSATION.LABEL_ID.eq(labelId))
        ).execute()
    }

    fun deleteUserConversations(userId: Int): Int {
        return context.deleteFrom(CONVERSATION)
            .where(CONVERSATION.CUSTOMER_ID.eq(userId))
            .execute()
    }
}
