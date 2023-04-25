package com.innovattic.medicinfo.logic.salesforce

import org.springframework.http.ResponseEntity
import kotlin.reflect.KClass

interface SalesforceClient {

    fun <T : Any> get(url: String, responseType: KClass<T>): ResponseEntity<T>

    fun <T : Any> post(url: String, body: Any, responseType: KClass<T>): ResponseEntity<T>
}
