package com.innovattic.medicinfo.web

import org.junit.jupiter.api.Test

class TriageJsonTest : BaseEndpointTest() {

    /**
     * Make sure all schema versions in the resources folder are parseable.
     */
    @Test
    fun validateAllSchemaVersions() {
        questionSchemaService.getAllSchemaFiles().forEach {
            questionSchemaService.readToInternalModel(it, 0)
        }
    }

}
