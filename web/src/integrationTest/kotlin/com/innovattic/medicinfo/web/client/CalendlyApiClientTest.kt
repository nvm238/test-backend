package com.innovattic.medicinfo.web.client

import com.innovattic.medicinfo.logic.calendly.CalendlyApiClient
import com.innovattic.medicinfo.test.BaseIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CalendlyApiClientTest : BaseIntegrationTest() {

    private val calendlyApiClient: CalendlyApiClient = CalendlyApiClient("apiKey", "signingKey")

    @Test
    fun `does not throw when signature valid`() {
        val validSignatureForSigningKeyAndPayload = "96b4e877f68bcdf0b2fc6bc8c652207a07d123cac5fcc330cc9e449475d6be20"

        Assertions.assertThatCode {
            calendlyApiClient.validateSignature("t=xxx,v1=$validSignatureForSigningKeyAndPayload", "mypayload")
        }.doesNotThrowAnyException()
    }

    @Test
    fun `throws exception when signature invalid`() {
        val inValidSignature = "inValidSignature"

        Assertions.assertThatCode {
            calendlyApiClient.validateSignature("t=xxx,v1=$inValidSignature", "mypayload")
        }.hasMessage("400 BAD_REQUEST \"Invalid signature\"")
    }
}