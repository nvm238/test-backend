package com.innovattic.medicinfo

import com.innovattic.common.notification.DevNotificationService
import com.innovattic.common.notification.SnsService
import com.innovattic.common.web.BaseApplication
import com.innovattic.common.web.IpWhitelistFilter
import com.innovattic.common.web.SecurityHeadersFilter
import com.innovattic.common.web.startSpringBootApplication
import com.innovattic.medicinfo.logic.calendly.CalendlyApi
import com.innovattic.medicinfo.logic.calendly.CalendlyApiClient
import com.innovattic.medicinfo.logic.calendly.DevCalendlyApiClient
import com.innovattic.medicinfo.logic.localazy.LocalazyClient
import com.innovattic.medicinfo.logic.salesforce.DevSalesforceClient
import com.innovattic.medicinfo.logic.salesforce.RealSalesforceClient
import com.innovattic.medicinfo.logic.salesforce.RealSalesforceService
import com.innovattic.medicinfo.logic.salesforce.SalesforceClient
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import com.innovattic.medicinfo.web.AppExceptionHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.task.TaskExecutorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import javax.servlet.http.HttpServletRequest

@EnableScheduling
@SpringBootApplication(scanBasePackages = ["com.innovattic.medicinfo"])
@ConfigurationPropertiesScan
open class Application : BaseApplication() {

    companion object {
        const val SWAGGER_URL = "/api/swagger-ui"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    open fun securityHeaders() = object : SecurityHeadersFilter() {
        override fun isSwagger(request: HttpServletRequest) = request.requestURI.startsWith(SWAGGER_URL)
    }

    /**
     * IP whitelist for Swagger documentation.
     * Access to a specific IP can be added by adding the IP to swagger.ipwhitelist in application-acc.properties
     * or application-prod.properties. The Innovattic office (/vpn) is always whitelisted.
     */
    @Bean
    open fun ipWhitelistFilter(
        exceptionHandler: AppExceptionHandler,
        @Value("\${swagger.ipwhitelist:}") ipWhitelist: List<String>
    ): IpWhitelistFilter {
        log.info("Using IP whitelist for swagger: ${ipWhitelist.joinToString(" ")}")
        return IpWhitelistFilter.forUrlPrefix(
            SWAGGER_URL,
            exceptionHandler,
            ipWhitelist.plus(IpWhitelistFilter.INNOVATTIC_OFFICE_IP)
        )
    }

    @Bean @Profile("!aws")
    open fun devNotifications() = DevNotificationService()

    @Bean @Profile("aws")
    open fun snsNotifications() = SnsService()

    @Bean
    open fun salesforceService(
        salesforceClient: SalesforceClient
    ): SalesforceService {
        log.info("Running with real Salesforce service")
        return RealSalesforceService(salesforceClient)
    }

    @Bean
    open fun salesforceClient(
        @Value("\${medicinfo.salesforce.url:}") url: String?,
        @Value("\${medicinfo.salesforce.apikey:}") apiKey: String?,
        @Value("\${medicinfo.salesforce.request.timeout-seconds:60}") timeoutSeconds: Long
    ): SalesforceClient {
        if (url.isNullOrEmpty() || apiKey.isNullOrEmpty()) {
            log.info("Salesforce URL or API key not configured: Creating salesforce client in dev mode")
            return DevSalesforceClient()
        }

        log.info("Salesforce client created using url=$url")
        return RealSalesforceClient(url, apiKey, timeoutSeconds)
    }

    @Bean
    open fun calendlyApi(
        @Value("\${calendly.api-key:}") apiKey: String?,
        @Value("\${calendly.callback.signature:}") webhookSigningKey: String,
    ): CalendlyApi {
        if (apiKey.isNullOrEmpty() || webhookSigningKey.isNullOrEmpty()) {
            log.info("Calendly API key or callback signature not configured: Running in dev mode")
            return DevCalendlyApiClient()
        }
        log.info("Running with real CalendlyApi")
        return CalendlyApiClient(apiKey, webhookSigningKey)
    }

    @Bean @Profile("!test")
    open fun localazyClient(
        @Value("\${localazy.bearer-token}") bearerToken: String,
    ) = LocalazyClient(bearerToken)

    /**
     * Normally, spring-boot auto-configures a thread pool executor. But because
     * spring-websockets uses custom executors, default configuration doesn't happen
     * and we need to define an executor ourselves. Without this, spring will use
     * SimpleAsyncTaskExecutor which is not meant for production use (warnings will appear).
     */
    @Bean(name = [APPLICATION_TASK_EXECUTOR_BEAN_NAME])
    open fun asyncExecutor(builder: TaskExecutorBuilder): ThreadPoolTaskExecutor {
        return builder.build()
    }
}

fun main(args: Array<String>) {
    startSpringBootApplication(Application::class, args)
}
