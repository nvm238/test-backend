package com.innovattic.medicinfo.database.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.innovattic.common.dto.EnumHelper
import com.innovattic.common.dto.EnumWithValue

enum class TriageProgress(
    val isStopped: Boolean,
    /**
     * Situation that this field is read when value is null should be treated as exception
     * as we cannot send statuses that represent triage in progress
     */
    val shouldBeSentToSalesforce: Boolean?,
    val isAbandoned: Boolean
) : EnumWithValue {
    STARTED(false, null, false),

    /**
     * All questions answered, not stopped by urgencies.
     */
    FINISHED(false, true, false),

    /**
     * Triage was started but not finished. It was just abandoned
     */
    FORCE_FINISHED(false, false, true),

    /**
     * When triage was prematurely ended because of an urgency, and user was sent to chat with a nurse.
     */
    FINISHED_BY_CHAT(false, true, false),

    /**
     * When user stopped the triage manually and want to chat with a nurse
     */
    FINISHED_BY_USER_WITH_CHAT(true, true, true),

    /**
     * When user stopped the triage manually, but does NOT want to chat with nurse
     */
    FINISHED_BY_USER_WITHOUT_CHAT(true, false, true),

    /**
     * DEPRECATED: Use FINISHED_BY_USER_OTHER
     * When user stopped the triage by choosing and option indicating that his pain does not fit any medical area present
     * In dutch: `mijn klacht staat er niet bij`
     */
    FINISHED_BY_USER_NO_MEDAREA(true, true, false),

    /**
     * Whe user finished the OVERIGE questionnaire this is the new way to handle no medical area selected.
     */
    FINISHED_BY_USER_OTHER(true, true, false),

    /**
     * Stopped manually, nurse has sent a link, triage is continued (with the medical area question pre-filled)
     */
    CONTINUED_AFTER_STOP(false, null, false),

    /**
     * When answer to a question ex. TRIAGEOTHER_AUTHORIZED means that we cannot legally continue
     */
    FINISHED_UNAUTHORIZED(false, false, false),

    /**
     * Triage is only accessible in Dutch, user that uses any other language should not be subject to triage
     */
    NOT_APPLICABLE(false, false, false),

    /**
     * User needs to pass EHIC information before starting the triage.
     */
    EHIC_REQUIRED(false, null, false),
    ;

    companion object : EnumHelper<TriageProgress>(TriageProgress::class) {
        @JvmStatic
        @JsonCreator
        fun get(value: String) = TriageProgress.fromValue(value)
    }
}
