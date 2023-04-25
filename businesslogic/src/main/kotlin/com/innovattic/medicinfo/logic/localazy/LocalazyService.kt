package com.innovattic.medicinfo.logic.localazy

import com.google.common.base.Suppliers
import com.innovattic.common.error.failResponseIf
import com.innovattic.medicinfo.logic.triage.model.QuestionDefinition
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LocalazyService(
    @Value("\${localazy.minutes-cached}") private val amountOfMinutesCached: Long,
    private val localazyClient: LocalazyClient,
) {

    fun getTranslation(langCode: String, key: String): String {
        if (key.isBlank()) return key
        val trimmedKey = key.trim()
        val parsedLangCode = parseLanguageCode(langCode)
        val translationFile = languageMap[parsedLangCode.lowercase()]
        val translation = translationFile?.get(trimmedKey)
        failResponseIf(translation == null, HttpStatus.NOT_FOUND) {
            "No translation found for the language: ${langCode.lowercase()} and key: $trimmedKey"
        }
        return translation
    }

    fun translateQuestionDefinition(langCode: String, questionDefinition: QuestionDefinition): QuestionDefinition {
        if (questionDefinition.isTranslated) return questionDefinition

        return questionDefinition.copy(
            question = getTranslation(langCode, questionDefinition.question),
            questionForCaregiver = getTranslation(langCode, questionDefinition.questionForCaregiver),
            additionalInfo = getTranslation(langCode, questionDefinition.additionalInfo),
            answer = questionDefinition.answer.map {
                it.copy(
                answerText = getTranslation(langCode, it.answerText),
            )
            },
            isTranslated = true
        )
    }

    private fun parseLanguageCode(langCode: String): String {
        return langCode.split('-')[0].lowercase()
    }

    private val dutchTranslations = Suppliers.memoizeWithExpiration({
        getTranslationFile("nl")
    }, amountOfMinutesCached, TimeUnit.MINUTES).get()

    private val germanTranslations = Suppliers.memoizeWithExpiration({
        getTranslationFile("de")
    }, amountOfMinutesCached, TimeUnit.MINUTES).get()

    private val englishTranslations = Suppliers.memoizeWithExpiration({
        getTranslationFile("en")
    }, amountOfMinutesCached, TimeUnit.MINUTES).get()

    private val languageMap = mapOf(
        "nl" to dutchTranslations,
        "de" to germanTranslations,
        "en" to englishTranslations,
    )

    private fun getTranslationFile(langCode: String): Map<String, String> =
        localazyClient.getTranslations(langCode)
}
