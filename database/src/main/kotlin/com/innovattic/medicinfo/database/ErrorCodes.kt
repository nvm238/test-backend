package com.innovattic.medicinfo.database

object ErrorCodes {
    const val DUPLICATE_CODE = "duplicate_code"
    const val DUPLICATE_ENTRY = "duplicate_entry"
    const val DUPLICATE_EMAIL = "duplicate_email"
    const val CONTENT_TYPE = "unsupported_content_type"
    const val UPLOAD_LIMIT_REACHED = "upload_limit_reached"
    const val CONVERSATION_EXPIRED = "conversation_expired"
    const val APPOINTMENT_ALREADY_BOOKED = "appointment_already_booked"
    const val APPOINTMENT_ALREADY_CANCELED = "appointment_already_canceled"
    const val USER_ALREADY_MIGRATED = "user_already_migrated"
    const val INVALID_MIGRATION = "invalid_migration"
    const val COV_INCORRECT = "cov_check_incorrect"
    const val CONVERSATION_MISSING = "conversation_missing"
}
