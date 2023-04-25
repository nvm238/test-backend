package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.fetchPojos
import com.innovattic.common.database.fetchSinglePojo
import com.innovattic.medicinfo.dbschema.tables.ReportingTriage.REPORTING_TRIAGE
import com.innovattic.medicinfo.dbschema.tables.ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER
import com.innovattic.medicinfo.dbschema.tables.pojos.ReportingTriage
import com.innovattic.medicinfo.dbschema.tables.pojos.ReportingTriageAnswer
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class TriageReportingDao(
    private val context: DSLContext
) {

    fun getTriage(id: Int): ReportingTriage {
        return context
            .selectFrom(REPORTING_TRIAGE)
            .where(REPORTING_TRIAGE.ID.eq(id))
            .fetchSinglePojo()
    }

    fun getAnswers(triageId: Int): List<ReportingTriageAnswer> {
        return context
            .selectFrom(REPORTING_TRIAGE_ANSWER)
            .where(REPORTING_TRIAGE_ANSWER.TRIAGE_ID.eq(triageId))
            .fetchPojos()
    }

    fun saveTriage(reportingTriage: ReportingTriage) {
        context
            .newRecord(REPORTING_TRIAGE, reportingTriage)
            .store()
    }

    fun saveAnswers(answers: List<ReportingTriageAnswer>) {
        // if this becomes a performance issue, we could rewrite to a bulk insert
        answers.forEach {
            context
                .newRecord(REPORTING_TRIAGE_ANSWER, it)
                .store()
        }
    }

    /**
     * Cleanup any previously reported data for the given triage status id.
     */
    fun cleanExisting(triageStatusId: Int) {
        context.deleteFrom(REPORTING_TRIAGE)
            .where(REPORTING_TRIAGE.ID.eq(triageStatusId))
            .execute()
        context.deleteFrom(REPORTING_TRIAGE_ANSWER)
            .where(REPORTING_TRIAGE_ANSWER.TRIAGE_ID.eq(triageStatusId))
            .execute()
    }
}
