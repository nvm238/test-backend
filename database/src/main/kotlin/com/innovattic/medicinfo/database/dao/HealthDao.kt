package com.innovattic.medicinfo.database.dao

import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
open class HealthDao(private val context: DSLContext) {
    fun check() {
        context.selectOne().execute()
    }
}
