package com.innovattic.medicinfo.logic

import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.logic.dto.OnlineEmployeeDto
import com.innovattic.medicinfo.logic.dto.ServiceAvailableDto
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import com.innovattic.medicinfo.logic.triage.MedicinfoServiceHoursProperties
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class OnlineEmployeeService(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val simpUserRegistry: SimpUserRegistry,
    private val salesforceService: SalesforceService,
    private val medicinfoServiceHoursProperties: MedicinfoServiceHoursProperties,
    private val clock: Clock,
    private val labelDao: LabelDao
    ) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * The last value that was received from Salesforce.
     * This value is updated periodically in another thread.
     *
     * In some cases, if salesforce is misbehaving, this value may be older than expected
     * (see https://innovattic.atlassian.net/browse/MEDSLA-135)
     */
    var latestValue: Int = 0
        private set

    @Scheduled(fixedDelay = 5000L)
    fun pollOnlineEmployees() {
        if (simpUserRegistry.findSubscriptions { it.destination == TOPIC_ROUTE }.isEmpty()) {
            log.debug("No users subscribed, skipping poll")
            return
        }

        val newValue = salesforceService.getOnlineEmployeeCount()
        if (latestValue != newValue) {
            pushNewValue(newValue)
        }
    }

    fun getServiceAvailability(labelCode: String): ServiceAvailableDto {
        val label = labelDao.getByCode(labelCode) ?: error("No label with code $labelCode")

        return medicinfoServiceHoursProperties.getServiceAvailability(clock, label.code)
    }

    fun pushNewValue(newValue: Int) {
        log.debug("Online employee count changed from $latestValue to $newValue")
        latestValue = newValue
        simpMessagingTemplate.convertAndSend(TOPIC_ROUTE, OnlineEmployeeDto(newValue))
    }

    companion object {
        const val TOPIC_ROUTE = "/v1/topic/online-employees"
    }
}
