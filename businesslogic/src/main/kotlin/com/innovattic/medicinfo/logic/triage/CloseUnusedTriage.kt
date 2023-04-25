package com.innovattic.medicinfo.logic.triage

import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dao.TriageStatusDao
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.logic.TriageService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class CloseUnusedTriage(
    private val triageStatusDao: TriageStatusDao,
    private val triageService: TriageService,
    private val conversationDao: ConversationDao
) {

    /**
     * We noticed that some triages on production were abandoned long time ago, thus are still marked as active.
     * Solution was needed to deactivate those old entries.
     * https://innovattic.atlassian.net/browse/MED-2246
     */
    @Scheduled(fixedDelay = 12, timeUnit = TimeUnit.HOURS)
    fun closeOldTriages() {
        triageStatusDao.getAllActiveOlderThen(48)
            .forEach {
                triageService.stopTriageWithReason(
                    triageStatus = it,
                    triageProgress = TriageProgress.FORCE_FINISHED,
                    reason = "Force finished stale triage"
                )
                val conversation = conversationDao.get(it.conversationId) ?: return@forEach
                conversationDao.archive(
                    conversationPublicId = conversation.publicId,
                    customerId = it.userId,
                    labelId = conversation.labelId
                )
            }
    }
}
