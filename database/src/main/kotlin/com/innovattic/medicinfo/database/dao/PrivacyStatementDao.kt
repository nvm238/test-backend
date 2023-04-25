package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.medicinfo.dbschema.Tables.PRIVACY_STATEMENT
import com.innovattic.medicinfo.dbschema.tables.pojos.PrivacyStatement
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class PrivacyStatementDao(private val context: DSLContext, private val clock: Clock) {

    fun getExistingOrCreate(userId: Int, version: String): PrivacyStatement {
        return findByUserAndVersion(userId, version) ?: this.create(userId, version)
    }

    fun create(userId: Int, version: String): PrivacyStatement {
        return context.insertRecord(PRIVACY_STATEMENT) {
            it.version = version
            it.acceptedAt = databaseNow(clock)
            it.userId = userId
        }.returningPojo()
    }

    fun findByUserAndVersion(userId: Int, version: String): PrivacyStatement? {
        return context.selectFrom(PRIVACY_STATEMENT)
            .where(
                PRIVACY_STATEMENT.USER_ID.eq(userId)
                .and(PRIVACY_STATEMENT.VERSION.eq(version))
            ).fetchOnePojo()
    }

    fun findLatestByUser(userId: Int): PrivacyStatement? {
        return context.selectFrom(PRIVACY_STATEMENT)
            .where(PRIVACY_STATEMENT.USER_ID.eq(userId))
            .orderBy(PRIVACY_STATEMENT.ACCEPTED_AT.desc())
            .limit(1)
            .fetchOnePojo()
    }
}
