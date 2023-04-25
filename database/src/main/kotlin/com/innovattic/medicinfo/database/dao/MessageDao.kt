package com.innovattic.medicinfo.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.common.database.OrderHelper
import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.fiqlQuery
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.medicinfo.database.dto.ActionDto
import com.innovattic.medicinfo.database.fiql.MessageQueryParser
import com.innovattic.medicinfo.dbschema.Tables.MESSAGE
import com.innovattic.medicinfo.dbschema.Tables.MESSAGE_VIEW
import com.innovattic.medicinfo.dbschema.tables.pojos.Message
import com.innovattic.medicinfo.dbschema.tables.pojos.MessageView
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Component
class MessageDao(
    private val context: DSLContext,
    private val clock: Clock,
    private val objectMapper: ObjectMapper
) {
    private val messageOrder = OrderHelper.Builder()
        .map("created", MESSAGE_VIEW.CREATED)
        .default(MESSAGE_VIEW.ID)
        .build()

    fun create(
        userId: Int,
        conversationId: Int,
        message: String,
        actionDto: ActionDto?,
        translatedMessage: String?
    ): Message {
        return context.insertRecord(MESSAGE) {
            it.userId = userId
            it.created = databaseNow(clock)
            it.conversationId = conversationId
            it.message = message
            it.translatedMessage = translatedMessage
            actionDto?.let { actionDto ->
                it.actionType = actionDto.type
                actionDto.context?.let { context ->
                    it.actionContext = objectMapper.writeValueAsString(context)
                }
            }
        }.returningPojo()
    }

    fun get(
        conversationId: Int,
        order: List<String>?,
        query: String?
    ): List<MessageView> {
        return context.select().from(MESSAGE_VIEW)
            .fiqlQuery(query, MessageQueryParser)
            .where(MESSAGE_VIEW.CONVERSATION_ID.eq(conversationId))
            .orderBy(messageOrder.parseOrders(order))
            .fetchInto(MessageView::class.java)
    }

    fun getMessageByUUID(uuid: UUID) = context.fetchOnePojo<Message>(
        MESSAGE,
        MESSAGE.PUBLIC_ID.eq(uuid)
    )

    fun updateMessage(message: Message): Message {
        return context.updateRecord(MESSAGE) {
            it.message = message.message
            it.translatedMessage = message.translatedMessage
        }.where(MESSAGE.ID.eq(message.id))
            .returningPojo()
    }

    fun getLatest(conversationId: Int): LocalDateTime? {
        return context.select(max(MESSAGE_VIEW.CREATED)).from(MESSAGE_VIEW)
            .where(MESSAGE_VIEW.CONVERSATION_ID.eq(conversationId))
            .fetchOneInto(LocalDateTime::class.java)
    }

    fun count(conversationId: Int, after: LocalDateTime?) =
        if (after != null) {
            context.fetchCount(
                MESSAGE,
                MESSAGE.CONVERSATION_ID.eq(conversationId).and(MESSAGE.CREATED.gt(after))
            )
        } else {
            context.fetchCount(
                MESSAGE,
                MESSAGE.CONVERSATION_ID.eq(conversationId)
            )
        }
}
