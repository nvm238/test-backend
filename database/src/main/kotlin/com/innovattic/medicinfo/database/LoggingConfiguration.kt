package com.innovattic.medicinfo.database

import com.innovattic.common.database.JooqLoggingListener
import org.jooq.ExecuteListenerProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
open class LoggingConfiguration {
    @Bean
    @Profile("!prod")
    open fun jooqLogging(): ExecuteListenerProvider {
        return ExecuteListenerProvider { JooqLoggingListener() }
    }
}
