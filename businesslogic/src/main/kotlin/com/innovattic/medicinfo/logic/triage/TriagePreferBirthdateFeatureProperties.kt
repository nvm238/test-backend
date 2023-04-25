package com.innovattic.medicinfo.logic.triage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "feature.triage")
data class TriagePreferBirthdateFeatureProperties(
    private var preferBirthdate: Map<String, Boolean>?,
) {
    fun preferBirthdateOverAgeForLabel(labelCode: String): Boolean = preferBirthdate?.get(labelCode) ?: true
}
