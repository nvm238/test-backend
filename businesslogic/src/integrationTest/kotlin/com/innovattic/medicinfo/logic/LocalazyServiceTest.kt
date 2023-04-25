package com.innovattic.medicinfo.logic

import com.innovattic.common.error.ResponseStatusWithCodeException
import com.innovattic.medicinfo.logic.localazy.LocalazyClient
import com.innovattic.medicinfo.logic.localazy.LocalazyService
import com.innovattic.medicinfo.test.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

class LocalazyServiceTest: BaseIntegrationTest() {

    @Test
    fun gettingDutchTranslationShouldCallLocalazyClient() {
        val localazyClient = Mockito.mock(LocalazyClient::class.java)
        Mockito.`when`(localazyClient.getTranslations("nl")).thenReturn(mapOf("test" to "test"))
        val localazyService = LocalazyService(1L, localazyClient)
        assertEquals(localazyService.getTranslation("nl","test"), "test")
    }

    @Test
    fun gettingDutchTranslationShouldCallLocalazyClientOnlyOnce() {
        val localazyClient = Mockito.mock(LocalazyClient::class.java)
        Mockito.`when`(localazyClient.getTranslations("nl")).thenReturn(mapOf("test" to "test"))
        val localazyService = LocalazyService(1L, localazyClient)
        assertEquals(localazyService.getTranslation("nl","test"), "test")

        // Change the translation on the "server" since the value is cached it should still return test
        Mockito.`when`(localazyClient.getTranslations("nl")).thenReturn(mapOf("test" to "anotherValue"))
        assertEquals(localazyService.getTranslation("nl","test"), "test")
    }

    @Test
    fun gettingTranslationWorksWithExpandedLanguageCode() {
        val localazyClient = Mockito.mock(LocalazyClient::class.java)
        Mockito.`when`(localazyClient.getTranslations("en")).thenReturn(mapOf("test" to "test"))
        val localazyService = LocalazyService(1L, localazyClient)
        // Were supplieng the expanded language code with capitals.
        // The service should only use the first value lowercased.
        assertEquals(localazyService.getTranslation("EN-GB","test"), "test")
    }

    @Test
    fun gettingTranslationThatDoesNotExistReturnsError() {
        val localazyClient = Mockito.mock(LocalazyClient::class.java)
        Mockito.`when`(localazyClient.getTranslations("de")).thenReturn(mapOf("test" to "test"))
        val localazyService = LocalazyService(1L, localazyClient)
        assertThrows<ResponseStatusWithCodeException> {  localazyService.getTranslation("de","nonExisting") }
    }
}