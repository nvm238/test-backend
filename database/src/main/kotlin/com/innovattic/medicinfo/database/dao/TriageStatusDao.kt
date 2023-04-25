package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.dbschema.Tables.LABEL
import com.innovattic.medicinfo.dbschema.Tables.TRIAGE_STATUS
import com.innovattic.medicinfo.dbschema.Tables.USER
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import org.jooq.DSLContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class TriageStatusDao(
    private val context: DSLContext,
    private val clock: Clock
) {

    fun getAllActiveOlderThen(hours: Long): List<TriageStatus> =
        context.selectFrom(TRIAGE_STATUS).where(
                TRIAGE_STATUS.ACTIVE.isTrue.and(
                    TRIAGE_STATUS.CREATED.lessThan(databaseNow(clock).minusHours(hours))
                )
            )
            .fetchInto(TriageStatus::class.java)

    fun getActiveByConversationId(conversationId: Int) =
        context.fetchOnePojo<TriageStatus>(
            TRIAGE_STATUS,
            TRIAGE_STATUS.CONVERSATION_ID.eq(conversationId).and(TRIAGE_STATUS.ACTIVE.isTrue)
        )

    fun getByConversationId(conversationId: Int) =
        context.fetchOnePojo<TriageStatus>(
            TRIAGE_STATUS,
            TRIAGE_STATUS.CONVERSATION_ID.eq(conversationId)
        )

    fun getById(triageStatusId: Int) =
        context.fetchOnePojo<TriageStatus>(
            TRIAGE_STATUS,
            TRIAGE_STATUS.ID.eq(triageStatusId)
        )

    fun createTriageStatus(userId: Int, schemaVersion: Int, conversationId: Int): TriageStatus {
        try {
            return context.insertRecord(TRIAGE_STATUS) {
                it.created = databaseNow(clock)
                it.userId = userId
                it.conversationId = conversationId
                it.status = TriageProgress.STARTED
                it.active = true
                it.schemaVersion = schemaVersion
            }.returningPojo()
        } catch (ignoreEx: DuplicateKeyException) {
            throw createResponseStatusException(code = ErrorCodes.DUPLICATE_ENTRY) {
                "Cannot create new triage for the userId: $userId. User already has an active triage"
            }
        }
    }

    fun setEhicRequiredStatus(id: Int): TriageStatus {
        return context.updateRecord(TRIAGE_STATUS) {
            it.status = TriageProgress.EHIC_REQUIRED
        }
            .where(TRIAGE_STATUS.ID.eq(id))
            .returningPojo()
    }

    fun startTriageStatus(id: Int): TriageStatus {
        return context.updateRecord(TRIAGE_STATUS) {
            it.status = TriageProgress.STARTED
        }
            .where(TRIAGE_STATUS.ID.eq(id))
            .returningPojo()
    }

    fun delete(id: Int) = context.delete(TRIAGE_STATUS).where(TRIAGE_STATUS.ID.eq(id)).execute()

    fun endTriageStatus(
        id: Int,
        status: TriageProgress,
        reason: String? = null
    ): TriageStatus {
        return context.updateRecord(TRIAGE_STATUS) {
            it.active = false
            it.ended = databaseNow(clock)
            it.status = status
            it.stopReason = reason
        }.where(TRIAGE_STATUS.ID.eq(id))
            .returningPojo()
    }

    fun continueTriage(id: Int, triageProgress: TriageProgress = TriageProgress.CONTINUED_AFTER_STOP): TriageStatus {
        return context.updateRecord(TRIAGE_STATUS) {
            it.active = true
            it.ended = null
            it.status = triageProgress
        }.where(TRIAGE_STATUS.ID.eq(id))
            .returningPojo()
    }

    fun findDistinctSchemaVersionForActive(): List<Int> = context.selectDistinct(TRIAGE_STATUS.SCHEMA_VERSION)
        .from(TRIAGE_STATUS)
        .where(TRIAGE_STATUS.ACTIVE.isTrue)
        .fetchInto(Int::class.java)

    fun getLabelCode(triageStatus: TriageStatus): String = context
        .select(LABEL.CODE)
        .from(USER)
        .join(LABEL).on(LABEL.ID.eq(USER.LABEL_ID))
        .where(USER.ID.eq(triageStatus.userId))
        .fetchOne()!!
        .value1()
}
