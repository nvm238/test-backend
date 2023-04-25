package com.innovattic.medicinfo.logic.dto.migration

import java.util.UUID
import javax.validation.constraints.NotNull

data class MigrateDto(
    @field:NotNull val userId: UUID? = null,
    @field:NotNull val conversationId: UUID ? = null
)
