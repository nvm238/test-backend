package com.innovattic.medicinfo.logic.eloqua

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "feature.eloqua")
data class EloquaConfigProperties(
    var trial: Map<String, EloquaProps>?
)

data class EloquaProps(val url: String, val siteId: String, val formName: String)
