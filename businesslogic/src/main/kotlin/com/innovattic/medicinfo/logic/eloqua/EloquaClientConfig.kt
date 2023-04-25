package com.innovattic.medicinfo.logic.eloqua

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class EloquaClientConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    open fun eloquaApiClients(eloquaProps: EloquaConfigProperties): Map<String, EloquaApiClient> {
        val labelsProps = eloquaProps.trial

        return labelsProps?.mapValues {
            log.info("Initializing eloqua configuration for: ${it.key}")
            EloquaApiClient(it.value.url, it.value.siteId, it.value.formName)
        } ?: emptyMap()
    }
}
