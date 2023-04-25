package com.innovattic.medicinfo.logic.dto.triage

data class ImageUploadResponse(
    /**
     * A unique identifier representing this image. This id can be used when answering a question with images.
     */
    val id: String
)
