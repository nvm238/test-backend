package com.innovattic.medicinfo.database.generator

import com.innovattic.common.database.converter.EnumConverter
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.database.dto.ActionType
import com.innovattic.medicinfo.database.dto.AppointmentType
import com.innovattic.medicinfo.database.dto.AttachmentType
import com.innovattic.medicinfo.database.dto.TriageProgress

class ConversationStatusConverter : EnumConverter<ConversationStatus>(ConversationStatus)
class GenderConverter : EnumConverter<Gender>(Gender)
class UserRoleConverter : EnumConverter<UserRole>(UserRole)
class ActionTypeConverter : EnumConverter<ActionType>(ActionType)
class AttachmentTypeConverter : EnumConverter<AttachmentType>(AttachmentType)
class TriageStatusConverter : EnumConverter<TriageProgress>(TriageProgress)
class AppointmentTypeConverter : EnumConverter<AppointmentType>(AppointmentType)
