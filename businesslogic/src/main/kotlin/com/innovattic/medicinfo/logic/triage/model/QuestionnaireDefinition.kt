package com.innovattic.medicinfo.logic.triage.model

data class QuestionnaireDefinition(
    val id: String,
    val displayName: String,
    val questions: List<QuestionDefinition>,
)
