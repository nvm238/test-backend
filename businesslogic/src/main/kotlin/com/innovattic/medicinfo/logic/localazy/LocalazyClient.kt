package com.innovattic.medicinfo.logic.localazy

import com.innovattic.medicinfo.logic.dto.localazy.LocalazyListFileContentResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import kotlin.reflect.KClass

class LocalazyClient(
    private val bearerToken: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    companion object {
        private const val BASE_URL = "https://api.localazy.com"
        const val PROJECT_ID = "_a7843007510235042395"
        const val FILE_ID = "_e685660101800"
    }

    /**
     * Returns a Map of key: value translations retrieved from the Localazy API.
     * The Localazy API returns translations a thousand records at a time. Therefore
     * the function loops over calls until no next value is given from the API. It
     * then returns the complete translation map.
     */
    fun getTranslations(langCode: String): Map<String, String> {
        val translations = mutableMapOf<String, String>()
        var nextPageId: String? = null
        while (true) {
            val response =
                call(
                    HttpMethod.GET,
                    "projects/$PROJECT_ID/files/$FILE_ID/keys/$langCode?deprecated=false&extra-info=false&next=$nextPageId",
                    null,
                    LocalazyListFileContentResponse::class
                )
            response.body?.keys?.let { keys -> translations.putAll(keys.associate { Pair(it.key.first(), it.value) }) }
            nextPageId = response.body?.next
            if (nextPageId.isNullOrEmpty()) break
        }
        return translations
    }

    private fun <T : Any> call(
        method: HttpMethod,
        url: String,
        body: Any?,
        responseType: KClass<T>
    ): ResponseEntity<T> {
        val fullUrl = "$BASE_URL/$url"
        val headers = HttpHeaders()
        headers.setBearerAuth(bearerToken)

        log.info("Localazy API call: $method $fullUrl")

        val response = try {
            restTemplate.exchange(fullUrl, method, HttpEntity(body, headers), responseType.java)
        } catch (e: RestClientResponseException) {
            log.error(
                "Localazy API call: $method $fullUrl returned with status code: ${e.rawStatusCode} and status text: ${e.statusText}"
            )
            error("Request to Localazy API Failed: $e")
        }

        log.info(
            "Localazy API call: $method $fullUrl returned with status code: ${response.statusCode} and body: ${response.body}"
        )

        return response
    }
}
