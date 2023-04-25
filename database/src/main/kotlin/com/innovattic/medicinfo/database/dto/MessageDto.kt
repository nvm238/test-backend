package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZonedDateTime
import java.util.UUID
import javax.validation.constraints.Null

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class MessageDto(
    @field:Null val userId: UUID? = null,
    @field:Null val userName: String? = null,
    @field:Null val userRole: UserRole? = null,
    @field:Null val id: UUID? = null,
    @field:Null val created: ZonedDateTime? = null,
    val message: String? = null,
    @field:Null val translatedMessage: String? = null,
    val action: ActionDto? = null,
    val attachment: AttachmentDto? = null,
)

data class ActionDto(
    val type: ActionType,
    val context: Map<String, Any>? = null,
)

data class AttachmentDto(
    val url: String,
    val type: AttachmentType
)

object TranslationCombineUtil {
    private const val DUTCH = "nl"
    private const val ENGLISH = "en"
    private const val GERMAN = "de"
    private const val RUSSIAN = "ru"
    private const val UKRAINIAN = "uk"

    private val separatorMap = mapOf(
        ENGLISH to "<This message is automatically translated to English>",
        GERMAN to "<Diese Nachricht wird automatisch ins Deutsche übersetzt>",
        RUSSIAN to "<Это сообщение автоматически переводится на русский язык>",
        UKRAINIAN to "<Це повідомлення автоматично перекладається українською мовою>",
    )

    fun combine(message: String, translatedMessage: String?, language: String?): String {
        if (translatedMessage.isNullOrBlank() || language.isNullOrBlank() || language == DUTCH) {
            return message
        }
        val separator = separatorMap.getOrDefault(language, separatorMap[ENGLISH])

        return "$message\n \n$separator\n \n$translatedMessage".trim()
    }
}
