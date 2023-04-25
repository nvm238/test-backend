package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.medicinfo.logic.dto.migration.MigrationService
import com.innovattic.medicinfo.logic.dto.migration.MigrateDto
import com.innovattic.medicinfo.logic.dto.migration.MigratedDto
import io.swagger.v3.oas.annotations.Operation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("v1/migrate")
class MigrationEndpoint(
    private val service: MigrationService,
    private val environment: Environment,
) : BaseEndpoint() {

    companion object {
        // TESTING ONLY - test migration by overwriting the existing api key for a user
        // this allows a user to be 'migrated' multiple times, or to migrate a user that actually was never migrated
        // from infosupport in the first place.
        const val HEADER_IGNORE_EXISTING_APIKEY = "X-IgnoreExistingApiKey"
    }

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @Operation(summary = "Migrate a user")
    fun migrateUser(@RequestBody @Valid dto: MigrateDto, req: HttpServletRequest): MigratedDto {

        var replaceApiKey = false

        // =========================================================================
        // MIGRATION TESTING only - for external parties trying their migration code
        val headerValue = req.getHeader(HEADER_IGNORE_EXISTING_APIKEY)
        if ("prod" !in environment.activeProfiles && headerValue != null) {
            log.info("Allowing re-migration for user {} conversation {}", dto.userId, dto.conversationId)
            replaceApiKey = true
        }
        // =========================================================================

        return service.migrateUser(dto, replaceApiKey)
    }
}
