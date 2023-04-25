package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.medicinfo.dbschema.Tables.APP_SELF_TEST
import com.innovattic.medicinfo.dbschema.tables.pojos.AppSelfTest
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class AppSelfTestDao(private val context: DSLContext) {

    fun getByLabelId(labelId: Int): AppSelfTest? =
        context.fetchOnePojo(APP_SELF_TEST, APP_SELF_TEST.LABEL_ID.eq(labelId))

    fun get(id: Int) = context.fetchOnePojo<AppSelfTest>(APP_SELF_TEST, APP_SELF_TEST.ID.eq(id))

    fun create(labelId: Int, data: String) {
        return context.insertRecord(APP_SELF_TEST) {
            it.labelId = labelId
            it.data = data
        }.returningPojo()
    }

    fun update(id: Int, data: String): Int {
        return context.updateRecord(APP_SELF_TEST) {
            it.data = data
        }.where(
            APP_SELF_TEST.ID.eq(id)
        ).execute()
    }
}
