package com.innovattic.medicinfo.logic

import com.innovattic.medicinfo.test.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.io.ByteArrayInputStream

class TriageImageServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var imageService: TriageImageService

    private fun createStream() = ByteArrayInputStream("test".toByteArray())
    private val contentType = "image/png"
    private val labelCode = "testlabel"

    @Test
    fun testUploadThenCleanup() {

        val triageId = 100
        val id1 = imageService.upload(labelCode, triageId, contentType, createStream())
        val id2 = imageService.upload(labelCode, triageId, contentType, createStream())
        val id3 = imageService.upload(labelCode, triageId, contentType, createStream())

        assertTrue(imageService.exist(labelCode, triageId, id1))
        assertTrue(imageService.exist(labelCode, triageId, id2))
        assertTrue(imageService.exist(labelCode, triageId, id3))

        // final answer contains only id2
        imageService.cleanup(labelCode, triageId, setOf(id2))

        assertFalse(imageService.exist(labelCode, triageId, id1))
        assertTrue(imageService.exist(labelCode, triageId, id2))
        assertFalse(imageService.exist(labelCode, triageId, id3))
    }

}