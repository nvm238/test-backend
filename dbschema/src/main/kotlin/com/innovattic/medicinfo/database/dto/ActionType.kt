package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class ActionType : EnumWithValue {
    CONFIRM_ID_DATA,
    CONFIRM_APPOINTMENT,
    OPEN_FACE_TALK,
    VIDEO_CHAT_MESSAGE,
    CREATE_APPOINTMENT,
    SURVEY,
    TRIAGE
    ;

    companion object : EnumHelper<ActionType>(ActionType::class) {
        @JvmStatic
        @JsonCreator
        fun get(value: String) = fromValue(value)

        const val VIDEO_CHAT_MESSAGE_APPOINTMENT_EXTERNAL_ID: String = "appointmentId"
        const val VIDEO_CHAT_MESSAGE_APPOINTMENT_ID: String = "appointmentPublicId"

        // Action type context documentation shown in Swagger. Please keep apidoc/salesforce.md in sync.
        const val ACTION_TYPE_CONTEXT_DESCRIPTION_DOC: String = """
### Valid action context properties per action type:
* confirm_appointment:
  - buttonText: String
* video_chat_message:
  - buttonText: String
  - url: String
  - startDateTime: String
  - appointmentId: String? *(When receiving messages from the server, this indicates the external identifier of the 
linked appointment. This id is not usable by customer apps; use the appointmentPublicId instead. When **creating** 
a message, this property should be set with the linked appointment (employee-only))*
  - appointmentPublicId: String? *(When **receiving** messages from the server, this property indicates the id of the 
linked appointment. Do not set this field when creating a message.)*
* create_appointment:
  - buttonText: String
* open_face_talk
* confirm_id_data
* survey
* triage:
  - buttonText: String
  - complaintAreaTriage: String

""" // .trimIndent is not available as the string should be available at compile time.
    }
}
