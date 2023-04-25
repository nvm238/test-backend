package com.innovattic.medicinfo.test

import com.innovattic.common.test.TestClock
import com.innovattic.medicinfo.logic.eloqua.EloquaApiClient
import com.innovattic.medicinfo.logic.localazy.LocalazyClient
import com.innovattic.medicinfo.logic.salesforce.DevSalesforceClient
import com.innovattic.medicinfo.logic.salesforce.SalesforceClient
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.Clock

@TestConfiguration
open class TestConfig {

    @Primary
    @Bean
    open fun clock(): Clock {
        return TestClock()
    }

    @Primary
    @Bean
    open fun salesforceService(salesforceClient: SalesforceClient): SalesforceService = MockSalesforceService(salesforceClient)

    @Primary
    @Bean
    open fun salesforceClient(): SalesforceClient = spy(DevSalesforceClient())

    @Primary
    @Bean
    open fun mockEloquaApiClients(): Map<String, EloquaApiClient> {
        return mapOf("CZdirect" to mock())
    }

    @Bean
    @Profile("test")
    open fun localazyClient() = Mockito.mock(LocalazyClient::class.java)
}
