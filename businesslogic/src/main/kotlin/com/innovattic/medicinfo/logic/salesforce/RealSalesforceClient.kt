package com.innovattic.medicinfo.logic.salesforce

import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientResponseException
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class RealSalesforceClient(
    private val baseUrl: String,
    private val apiKey: String,
    timeoutSeconds: Long
) : SalesforceClient {
    private val log = LoggerFactory.getLogger(javaClass)
    private val activeSalesforceCalls = AtomicInteger(0)
    private val restTemplate = RestTemplateBuilder()
        .setConnectTimeout(Duration.ofSeconds(timeoutSeconds))
        .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
        .build()

    override fun <T : Any> get(url: String, responseType: KClass<T>) =
        call(HttpMethod.GET, url, null, responseType)

    override fun <T : Any> post(url: String, body: Any, responseType: KClass<T>) =
        call(HttpMethod.POST, url, body, responseType)

    private fun <T : Any> call(
        method: HttpMethod,
        url: String,
        body: Any?,
        responseType: KClass<T>
    ): ResponseEntity<T> {
        val headers = HttpHeaders()
        headers.set("Ocp-Apim-Subscription-Key", apiKey)
        val fullUrl = "$baseUrl/$url"

        // don't log the request body; it can contain privacy-sensitive information as well as very big chunks
        // of data (images)
        log.info(
            "$method $fullUrl ({} ongoing salesforce requests)",
            activeSalesforceCalls.incrementAndGet()
        )

        val startTime = System.currentTimeMillis()
        val response = try {
            restTemplate.exchange(fullUrl, method, HttpEntity(body, headers), responseType.java)
        } catch (e: RestClientResponseException) {
            val durationMs = System.currentTimeMillis() - startTime
            val ongoingSalesforceRequestCount = activeSalesforceCalls.decrementAndGet()
            log.error(
                """$method $fullUrl returned with status ${e.rawStatusCode} and status text ${e.statusText} 
            | and body {}, request took {} ms ({} ongoing salesforce requests)
            """.trimMargin(),
                e.responseBodyAsString, durationMs, ongoingSalesforceRequestCount
            )
            error("Request to salesforce logic apps failed: $e")
        } catch (e: Exception) {
            val ongoingSalesforceRequestCount = activeSalesforceCalls.decrementAndGet()

            log.error("An error has occurred ({} ongoing salesforce requests): {}", ongoingSalesforceRequestCount, e)
            error("Request to salesforce logic apps failed: $e")
        }
        val durationMs = System.currentTimeMillis() - startTime

        log.info(
            "$method $fullUrl returned with status ${response.statusCodeValue} and body {}, request took {} ms " +
                "({} ongoing salesforce requests)",
            response.body, durationMs, activeSalesforceCalls.decrementAndGet()
        )

        return response
    }
}
