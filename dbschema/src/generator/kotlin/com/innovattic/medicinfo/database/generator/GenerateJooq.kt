package com.innovattic.medicinfo.database.generator

import com.innovattic.common.database.DatabaseConnectionProperties
import com.innovattic.common.database.generator.JooqGenerator
import com.innovattic.common.database.generator.enumType
import com.innovattic.medicinfo.database.converter.JSONBConverter
import org.jooq.meta.jaxb.ForcedType

fun main(args: Array<String>) {
    val connection = DatabaseConnectionProperties.fromCommandLineArgs(args)
    JooqGenerator(
        connection, "com.innovattic.medicinfo.dbschema",
        enumType("conversation.status", ConversationStatusConverter()),
        enumType("user.gender", GenderConverter()),
        enumType("user_view.gender", GenderConverter()),
        enumType("user.role", UserRoleConverter()),
        enumType("user_view.role", UserRoleConverter()),
        enumType("message.action_type", ActionTypeConverter()),
        enumType("message_attachment.attachment_type", AttachmentTypeConverter()),
        enumType("message_view.attachment_content_type", AttachmentTypeConverter()),
        enumType("message_view.action_type", ActionTypeConverter()),
        enumType("triage_status.status", TriageStatusConverter()),
        enumType("reporting_triage.triage_state", TriageStatusConverter()),
        enumType("calendly_appointment.appointment_type", AppointmentTypeConverter()),
        ForcedType()
            .withIncludeExpression("message_view.action_context")
            .withUserType(String::class.java.typeName)
            .withConverter(JSONBConverter::class.qualifiedName),
        ForcedType()
            .withIncludeExpression("message.action_context")
            .withUserType(String::class.java.typeName)
            .withConverter(JSONBConverter::class.qualifiedName),
        ForcedType()
            .withIncludeExpression("app_self_test.data")
            .withUserType(String::class.java.typeName)
            .withConverter(JSONBConverter::class.qualifiedName),
        ForcedType()
            .withIncludeExpression("user_app_self_test_result.data")
            .withUserType(String::class.java.typeName)
            .withConverter(JSONBConverter::class.qualifiedName)
    ).run()
}
