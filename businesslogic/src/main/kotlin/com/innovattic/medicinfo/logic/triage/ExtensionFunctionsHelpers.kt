package com.innovattic.medicinfo.logic.triage

import com.innovattic.medicinfo.database.dto.TriageProgress.CONTINUED_AFTER_STOP
import com.innovattic.medicinfo.database.dto.TriageProgress.NOT_APPLICABLE
import com.innovattic.medicinfo.database.dto.TriageProgress.STARTED
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus

fun TriageStatus.isContinued() = status == CONTINUED_AFTER_STOP
fun TriageStatus.isStarted() = status == STARTED
fun TriageStatus.isRestartStatus() = status != NOT_APPLICABLE
