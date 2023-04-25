package com.innovattic.medicinfo.web.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InfoVersionDto(
    val commitId: String?,
    val tagName: String?,
    val branchName: String?
)
