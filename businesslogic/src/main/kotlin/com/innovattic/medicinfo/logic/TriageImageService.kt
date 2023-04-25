package com.innovattic.medicinfo.logic

import com.innovattic.common.file.FileService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.*

data class Download(
    val contentType: String,
    val stream: InputStream,
)

/**
 * Handles the image uploading for triage.
 *
 * Images are uploaded before answering a question. At that point, they are stored on s3 with a generated unique id.
 * These ids can be used when answering a question.
 *
 * After finishing a questionnaire (either by completing it or by aborting it), [cleanup] should be called to remove
 * any uploaded files that were not used in the triage answers. This can happen, for example, when answering a question
 * and then going back to re-answer it with different images.
 */
@Service
class TriageImageService(
    private val fileService: FileService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Upload a file to storage, keeping it linked to the given triage.
     *
     * @return an image identifier to be used for other calls to this class
     */
    fun upload(labelCode: String, triageStatusId: Int, contentType: String, input: InputStream): String {

        val fileId = UUID.randomUUID().toString()
        val storageKey = storageKeyFor(labelCode, triageStatusId, fileId)

        // TODO: implement some high limit of files per triage, ie. max 100 uploads for 1 triage
        // to prevent server overloads (low priority)

        // upload file to s3
        log.info("Uploading image $storageKey for triage $triageStatusId")
        fileService.writeFile(storageKey, contentType, input)

        return fileId
    }

    /**
     * Download image.
     *
     * @return the image contents
     */
    fun download(labelCode: String, triageStatusId: Int, fileId: String): Download {
        val storageKey = storageKeyFor(labelCode, triageStatusId, fileId)
        val contentType = fileService.getContentType(storageKey)
        val contents = fileService.readFile(storageKey)
        return Download(contentType, contents)
    }

    fun exist(labelCode: String, triageStatusId: Int, imageId: String): Boolean {
        val key = storageKeyFor(labelCode, triageStatusId, imageId)
        return fileService.exists(key)
    }

    private fun storageKeyPrefixFor(labelCode: String, triageStatusId: Int): String {
        return "$labelCode/triage-$triageStatusId"
    }

    private fun storageKeyFor(labelCode: String, triageStatusId: Int, imageId: String): String {
        return storageKeyPrefixFor(labelCode, triageStatusId) + "/" + imageId
    }

    /**
     * Cleanup any uploaded images that are not used in the final triage answers.
     *
     * @param imageIdsToKeep a list of image ids that were used in triage when completed
     */
    fun cleanup(labelCode: String, triageStatusId: Int, imageIdsToKeep: Set<String>) {

        val existingFiles = fileService.listFiles(storageKeyPrefixFor(labelCode, triageStatusId) + "/")
        val filesToKeep = imageIdsToKeep.map { storageKeyFor(labelCode, triageStatusId, it) }.toSet()
        val filesToRemove = existingFiles - filesToKeep
        log.debug("Existing files: ${existingFiles.size}, filesToKeep ${filesToKeep.size}, filesToRemove ${filesToRemove.size}")

        filesToRemove.forEach {
            try {
                log.info("Removing uploaded image $it because it was not used in the triage after completion")
                fileService.deleteFile(it)
            } catch (e: Exception) {
                log.warn("Could not cleanup image $it", e)
            }
        }
    }
}
