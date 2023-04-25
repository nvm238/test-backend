package com.innovattic.medicinfo.database.converter

import org.jooq.Converter
import org.jooq.JSONB

class JSONBConverter : Converter<JSONB, String> {
    override fun from(databaseObject: JSONB?): String? {
        return databaseObject?.data()
    }

    override fun to(userObject: String?): JSONB? {
        return userObject?.let { JSONB.valueOf(it) }
    }

    override fun fromType(): Class<JSONB> {
        return JSONB::class.java
    }

    override fun toType(): Class<String> {
        return String::class.java
    }
}
