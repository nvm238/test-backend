package com.innovattic.medicinfo.file

import com.google.common.io.ByteStreams
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.imageio.ImageIO

object ImageFormatDetection {
    private val supportedMimeTypes = listOf("image/png", "image/jpeg")
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * This method checks whether image is in one of the supported formats(png, jpeg).
     * NOTE: this method does not close the input stream. It is a caller responsibility
     *
     * @param inputStream containing an image
     * @return if given input stream is an image in a supported format
     */
    fun isSupportedFormat(inputStream: InputStream): Boolean {
        val imageBytes = ByteStreams.toByteArray(inputStream)
        return isSupportedFormat(imageBytes)
    }

    /**
     * This method checks whether image is in one of the supported formats(png, jpeg)
     *
     * @param imageBytes byte array containing an image
     * @return if given byte array is an image in a supported format
     */
    fun isSupportedFormat(imageBytes: ByteArray): Boolean {
        supportedMimeTypes.forEach {
            val supports = supports(ByteArrayInputStream(imageBytes), it)
            if (supports) {
                return true
            }
        }

        return false
    }

    private fun supports(inputStream: InputStream, mimeType: String): Boolean {
        val readers = ImageIO.getImageReadersByMIMEType(mimeType)
        while (readers.hasNext()) {
            val reader = readers.next()
            val result = ImageIO.createImageInputStream(inputStream).use { iis ->
                reader.input = iis
                kotlin.runCatching { reader.read(0) }
            }
            reader.dispose()
            if (result.isSuccess) {
                return true
            } else {
                log.info("Image supported check failed, Exception: ${result.exceptionOrNull()}")
            }
            inputStream.reset()
        }

        return false
    }
}
