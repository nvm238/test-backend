package com.innovattic.medicinfo.database.dao

import com.innovattic.common.database.databaseNow
import com.innovattic.common.database.fetchOnePojo
import com.innovattic.common.database.fetchPojos
import com.innovattic.common.database.insertRecord
import com.innovattic.common.database.returningPojo
import com.innovattic.common.database.updateRecord
import com.innovattic.medicinfo.dbschema.Tables.TRIAGE_ANSWER
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageAnswer
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class TriageAnswerDao(
    private val context: DSLContext,
    private val clock: Clock
) {
    fun getLatestBy(triageStatusId: Int): TriageAnswer? =
        context.selectFrom(TRIAGE_ANSWER)
            .where(
                TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
            )
            .orderBy(TRIAGE_ANSWER.CREATED.desc(), TRIAGE_ANSWER.ID.desc())
            .limit(1)
            .fetchOnePojo()

    fun getPreviousByUniqueId(triageStatusId: Int, questionUniqueId: String): TriageAnswer? =
        context.selectFrom(TRIAGE_ANSWER)
            .where(
                TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
                    .and(
                        TRIAGE_ANSWER.ID.lessThan(
                            select(TRIAGE_ANSWER.ID).from(TRIAGE_ANSWER)
                                .where(
                                    TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
                                        .and(TRIAGE_ANSWER.QUESTION_ID.eq(questionUniqueId))
                                )
                        )
                    )
            )
            .orderBy(TRIAGE_ANSWER.ID.desc())
            .limit(1)
            .fetchOnePojo()

    fun getNextByUniqueId(triageStatusId: Int, questionUniqueId: String): TriageAnswer? =
        context.selectFrom(TRIAGE_ANSWER)
            .where(
                TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
                    .and(
                        TRIAGE_ANSWER.ID.greaterThan(
                            select(TRIAGE_ANSWER.ID).from(TRIAGE_ANSWER)
                                .where(
                                    TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
                                        .and(TRIAGE_ANSWER.QUESTION_ID.eq(questionUniqueId))
                                )
                        )
                    )
            )
            .orderBy(TRIAGE_ANSWER.ID)
            .limit(1)
            .fetchOnePojo()

    fun deleteAllWithIdGreaterOrEqualThan(triageStatusId: Int, questionUniqueId: String) =
        context.deleteFrom(TRIAGE_ANSWER)
            .where(
                TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
                    .and(
                        TRIAGE_ANSWER.ID.greaterOrEqual(
                            select(TRIAGE_ANSWER.ID).from(TRIAGE_ANSWER)
                                .where(
                                    TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
                                        .and(TRIAGE_ANSWER.QUESTION_ID.eq(questionUniqueId))
                                )
                        )
                    )
            ).execute()

    fun findByTriageStatusIdAndQuestionId(
        triageStatusId: Int,
        questionId: String
    ): TriageAnswer? =
        context.selectFrom(TRIAGE_ANSWER)
            .where(
                TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId)
                    .and(TRIAGE_ANSWER.QUESTION_ID.eq(questionId))
            )
            .fetchOnePojo()

    fun getAllByTriageStatusId(triageStatusId: Int): List<TriageAnswer> =
        context.selectFrom(TRIAGE_ANSWER)
            .where(TRIAGE_ANSWER.TRIAGE_STATUS_ID.eq(triageStatusId))
            .orderBy(TRIAGE_ANSWER.ID)
            .fetchPojos()

    fun saveNew(
        triageStatusId: Int,
        questionId: String,
        answerJson: String,
        rawAnswerJson: String
    ): TriageAnswer =
        context.insertRecord(TRIAGE_ANSWER) {
            it.created = databaseNow(clock)
            it.questionId = questionId
            it.answer = JSON.json(answerJson)
            it.rawAnswer = rawAnswerJson
            it.triageStatusId = triageStatusId
        }.returningPojo()

    fun updateTriageAnswer(
        id: Int,
        answerJson: String,
        rawAnswerJson: String
    ): TriageAnswer =
        context.updateRecord(TRIAGE_ANSWER) {
            it.created = databaseNow(clock)
            it.answer = JSON.json(answerJson)
            it.rawAnswer = rawAnswerJson
        }.where(TRIAGE_ANSWER.ID.eq(id)).returningPojo()
}
