package com.innovattic.medicinfo.logic.triage.tree

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innovattic.medicinfo.database.dao.TriageStatusDao
import com.innovattic.medicinfo.logic.triage.model.ModelMapper
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireModel
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class QuestionSchemaService(
    private val triageStatusDao: TriageStatusDao
) {

    private val schemaVersionList = sortedMapOf<Int, QuestionnaireModel>()

    @PostConstruct
    fun initMap() {
        schemaVersionList.clear()
        val resources = getAllSchemaFiles()
        val storedActiveSchemaVersions = triageStatusDao.findDistinctSchemaVersionForActive()
        val latestSchemaVersion = resources.maxOfOrNull(::parseVersion)
            ?: error("Resources are empty")
        for (resource in resources) {
            val resourceVersion = parseVersion(resource)
            if (latestSchemaVersion == resourceVersion || storedActiveSchemaVersions.contains(resourceVersion)) {
                val internalSchema = readToInternalModel(resource, resourceVersion)
                schemaVersionList[internalSchema.version] = internalSchema
            }
        }
        if (schemaVersionList.isEmpty()) {
            error("There is no question schemas registered!")
        }
    }

    fun getAllSchemaFiles() = PathMatchingResourcePatternResolver().getResources("classpath*:decisionTree/*.json")

    /**
     * Load a specific resource file into the version map.
     */
    fun loadFile(version: Int, filename: String): QuestionnaireModel {
        val resource = DefaultResourceLoader().getResource("classpath:$filename")
        val schema = readToInternalModel(resource, version)
        schemaVersionList[version] = schema
        return schema
    }

    fun readToInternalModel(resource: Resource, version: Int): QuestionnaireModel {
        val mapper = jacksonObjectMapper()
                // validate schema strictly, we should not have any missing properties in the json
                // (which are normally auto-mapped to a default value by jackson)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        val externalSchema = mapper.readValue(
            resource.inputStream,
            com.innovattic.medicinfo.logic.triage.model.external.QuestionnaireModel::class.java
        )
            ?: error("Parsed question schema is null!")

        return ModelMapper.mapExternalToInternal(externalSchema, version)
    }

    private fun parseVersion(resource: Resource): Int {
        val filename = resource.filename ?: error("Filename for schema is mandatory")
        return parseFilename(filename)
    }

    private fun parseFilename(filename: String): Int {
        val order = filename.substring(0, filename.indexOf("."))
        try {
            return order.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalStateException(
                "Filename for schema is in invalid format, valid format is <number>.json | filename=$filename",
                e
            )
        }
    }

    fun getQuestionSchema(version: Int): QuestionnaireModel? {
        return schemaVersionList[version]
    }

    fun getLatestSchema(): QuestionnaireModel {
        return schemaVersionList[schemaVersionList.lastKey()] ?: error("Last key is not present")
    }
}
