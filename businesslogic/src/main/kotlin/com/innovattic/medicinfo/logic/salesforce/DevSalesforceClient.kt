package com.innovattic.medicinfo.logic.salesforce

import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class DevSalesforceClient : SalesforceClient {
    private val log = LoggerFactory.getLogger(javaClass)

    private val activeSalesforceCalls = AtomicInteger(0)

    override fun <T : Any> get(url: String, responseType: KClass<T>) =
        call<T>(HttpMethod.GET, url)

    override fun <T : Any> post(url: String, body: Any, responseType: KClass<T>) =
        call<T>(HttpMethod.POST, url)

    private fun <T : Any> call(
        method: HttpMethod,
        url: String,
    ): ResponseEntity<T> {
        val fullUrl = "salesforce-dev-url.com/$url"

        log.info("$method $fullUrl ({} ongoing salesforce requests)", activeSalesforceCalls.incrementAndGet())

        return ResponseEntity(HttpStatus.OK)
    }
}
