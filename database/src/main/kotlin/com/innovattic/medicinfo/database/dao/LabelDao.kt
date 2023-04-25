package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.fetchPojos
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dto.LabelDto
import com.innovattic.medicinfo.dbschema.Tables.LABEL
import com.innovattic.medicinfo.dbschema.tables.pojos.Label
import org.jetbrains.annotations.TestOnly
import org.jooq.DSLContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.*

@Component
open class LabelDao(private val context: DSLContext, private val clock: Clock) {
    fun getById(id: Int) = context.fetchOnePojo<Label>(LABEL, LABEL.ID.eq(id))
    fun getByPublicId(id: UUID) = context.fetchOnePojo<Label>(LABEL, LABEL.PUBLIC_ID.eq(id))
    fun getByCode(code: String) = context.fetchOnePojo<Label>(LABEL, LABEL.CODE.eq(code))

    fun getList() = context.selectFrom(LABEL).orderBy(LABEL.ID).fetchPojos<Label>()

    fun create(dto: LabelDto): Label {
        try {
            return context.insertRecord(LABEL) {
                it.created = databaseNow(clock)
                it.code = dto.code
                it.name = dto.name
            }.returningPojo()
        } catch (ignoreEx: DuplicateKeyException) {
            throw createResponseStatusException(code = ErrorCodes.DUPLICATE_CODE) { "Label with code ${dto.code} already exists" }
        }
    }

    fun updateLabel(
        id: Int,
        name: String?,
        code: String?,
    ): Label {
        return context.updateRecord(LABEL) {
            it.name = name
            it.code = code
        }.where(LABEL.ID.eq(id)).returningPojo()
    }

    fun registerApiKey(id: Int, apiKey: String, arn: String) {
        context.updateRecord(LABEL) {
            it.fcmApiKey = apiKey
            it.snsApplicationArn = arn
        }.where(LABEL.ID.eq(id)).execute()
    }

    fun updatePushNotificationText(label: Label, pushNotificationText: String) {
        context.updateRecord(LABEL) {
            it.pushNotificationText = pushNotificationText
        }.where(LABEL.ID.eq(label.id!!)).execute()
    }

    fun delete(id: Int) {
        context.deleteFrom(LABEL).where(LABEL.ID.eq(id)).execute()
    }

    @TestOnly
    fun clear() {
        context.deleteFrom(LABEL).execute()
    }
}
