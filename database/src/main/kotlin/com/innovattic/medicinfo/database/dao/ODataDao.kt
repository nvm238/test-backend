package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.fetchOnePojo
import com.innovattic.medicinfo.dbschema.Tables
import com.innovattic.medicinfo.dbschema.tables.pojos.OdataCustomerView
import com.innovattic.medicinfo.dbschema.tables.pojos.OdataMessageView
import org.jetbrains.annotations.TestOnly
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
open class ODataDao(private val context: DSLContext) {

    @TestOnly
    fun getCustomerView(id: String): OdataCustomerView? {
        return context.selectFrom(Tables.ODATA_CUSTOMER_VIEW).where(Tables.ODATA_CUSTOMER_VIEW.CUSTOMER_ID.eq(id))
            .fetchOnePojo()
    }

    @TestOnly
    fun getMessageView(id: String): OdataMessageView? {
        return context.selectFrom(Tables.ODATA_MESSAGE_VIEW).where(Tables.ODATA_MESSAGE_VIEW.ID.eq(id)).fetchOnePojo()
    }
}
