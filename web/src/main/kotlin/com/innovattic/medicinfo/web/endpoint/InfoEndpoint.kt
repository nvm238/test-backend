package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.web.BaseHealthEndpoint
import com.innovattic.medicinfo.web.dto.InfoVersionDto
import com.innovattic.medicinfo.web.dto.AppHealthDto
import com.innovattic.medicinfo.database.dao.HealthDao
import io.swagger.v3.oas.annotations.Operation
import org.springframework.boot.info.GitProperties
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock

@RestController
@RequestMapping("info")
// When running in IntelliJ, spring-actuator's "info endpoint" is enabled by default, which has the same bean
// name as our endpoint - let's rename ours (has no effect on url mappings)
@Component("medicinfo-info-endpoint")
class InfoEndpoint(clock: Clock, private val healthDao: HealthDao, private val git: GitProperties?) : BaseHealthEndpoint(clock) {
    @GetMapping("health")
    @Operation(summary = "Check server health", description = Swagger.PERMISSION_PUBLIC)
    fun serverHealth(): ResponseEntity<AppHealthDto> {
        return ResponseEntity.ok(AppHealthDto(checkHealth("db", healthDao::check)))
    }

    @GetMapping("version")
    @Operation(summary = "Get server version", description = Swagger.PERMISSION_PUBLIC)
    fun getVersionInfo(): ResponseEntity<Any> {
        git ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(InfoVersionDto(git.commitId, git["tags"], git["branch"]))
    }
}
