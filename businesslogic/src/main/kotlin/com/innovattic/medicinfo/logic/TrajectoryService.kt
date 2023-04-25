package com.innovattic.medicinfo.logic

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class TrajectoryService(
    private val salesforceService: SalesforceService,
    private val conversationDao: ConversationDao,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun close(user: User) {
        val maybeLatestConversation = conversationDao.getLatest(user.id)
        try {
            salesforceService.closeSelfTestAsync(user, maybeLatestConversation?.publicId)
        } catch (expectedEx: RuntimeException) {
            log.warn("Sending close-selftest to salesforce failed", expectedEx)
            throw createResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE) {
                "Salesforce responded with an error: ${expectedEx.message}"
            }
        }
    }
}
