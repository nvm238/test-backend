package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.medicinfo.dbschema.Tables.USER_APP_SELF_TEST_RESULT
import com.innovattic.medicinfo.dbschema.tables.pojos.UserAppSelfTestResult
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class UserAppSelfTestResultDao(private val context: DSLContext) {

    fun get(customerId: Int): UserAppSelfTestResult? =
        context.fetchOnePojo(USER_APP_SELF_TEST_RESULT, USER_APP_SELF_TEST_RESULT.CUSTOMER_ID.eq(customerId))

    fun create(customerId: Int, labelId: Int, data: String) {
        return context.insertRecord(USER_APP_SELF_TEST_RESULT) {
            it.customerId = customerId
            it.labelId = labelId
            it.data = data
        }.returningPojo()
    }

    fun update(id: Int, data: String): Int {
        return context.updateRecord(USER_APP_SELF_TEST_RESULT) {
            it.data = data
        }.where(
            USER_APP_SELF_TEST_RESULT.ID.eq(id)
        ).execute()
    }
}
