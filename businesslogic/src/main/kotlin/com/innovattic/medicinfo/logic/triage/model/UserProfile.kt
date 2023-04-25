package com.innovattic.medicinfo.logic.triage.model

import com.innovattic.medicinfo.database.dto.Gender

data class UserProfile(
    val age: Int,
    val gender: Gender,
    val currentQuestionnaireName: String,
    /**
     * Indicates if this object represents user of the app or person that questionnaire
     * is filled for(based on questions from Profile questionnaire)
     */
    val isAppUser: Boolean,
    val labelCode: String,
    val entryType: String?
)
