package com.innovattic.medicinfo.web.endpoint.v1

import com.google.common.io.ByteStreams
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.file.ImageFormatDetection
import com.innovattic.medicinfo.logic.EhicService
import com.innovattic.medicinfo.logic.dto.EhicUploadResponse
import com.innovattic.medicinfo.web.dto.EmailDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("v1/ehic")
class EhicEndpoint(
    private val ehicService: EhicService
) : BaseEndpoint() {

    @PostMapping("image")
    @Operation(
        summary = "Post an image of the EHI Card for validation by salesforce.",
        description =
    """
        This endpoint is used to upload an image of the European Health Information Card (EHIC). This can be used
        by saleforce to handle payment processing.
        
        ${Swagger.PERMISSION_CUSTOMER}
    """
    ) fun addEhicImage(
        request: HttpServletRequest,
        @RequestHeader("Content-Type") contentType: String
    ): ResponseEntity<EhicUploadResponse> {
        verifyRole(UserRole.CUSTOMER)

        // very simple detection against bogus content types
        try {
            val parsedType = MediaType.parseMediaType(contentType)
            if (parsedType.type != "image") {
                throw createResponseStatusException { "Content-Type is invalid, needs to be an image type" }
            }
        } catch (ignoreEx: Exception) {
            throw createResponseStatusException { "Content-Type is invalid" }
        }

        val imageBytes = ByteStreams.toByteArray(request.inputStream)
        if (!ImageFormatDetection.isSupportedFormat(imageBytes)) {
            throw createResponseStatusException { "Image format not supported" }
        }

        return ResponseEntity.ok(ehicService.uploadEhicImage(queryAuthenticatedUser(), contentType, ByteArrayInputStream(imageBytes)))
    }

    @PostMapping("email")
    @Operation(
        summary = "Post an email address to handle payment info",
        description = """
            This endpoint is used to save an email address of the user to handle payment info.
        """
    )
    fun addEmail(@Valid @RequestBody dto: EmailDto): ResponseEntity<Void> {
        verifyRole(UserRole.CUSTOMER)
        ehicService.addEmail(queryAuthenticatedUser(), dto.email)
        return ResponseEntity.ok().build()
    }
}
