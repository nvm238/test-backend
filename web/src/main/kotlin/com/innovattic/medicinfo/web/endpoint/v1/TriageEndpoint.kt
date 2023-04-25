package com.innovattic.medicinfo.web.endpoint.v1

import com.google.common.io.ByteStreams
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.file.copyStream
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.file.ImageFormatDetection
import com.innovattic.medicinfo.logic.TriageService
import com.innovattic.medicinfo.logic.UserService
import com.innovattic.medicinfo.logic.dto.triage.AnswerRequest
import com.innovattic.medicinfo.logic.dto.triage.AnswerResponse
import com.innovattic.medicinfo.logic.dto.triage.ImageUploadResponse
import com.innovattic.medicinfo.logic.dto.triage.StopTriageRequest
import com.innovattic.medicinfo.logic.dto.triage.TriageStateQuestionResponse
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.LocaleUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.ByteArrayInputStream
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("v1/triage")
class TriageEndpoint(
    private val triageService: TriageService,
    private val userService: UserService,
) : BaseEndpoint() {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @Operation(
        summary = """Start a triage for the currently active conversation. 
            If there is no ongoing conversation, a  new one is started automatically.""",
        description = Swagger.PERMISSION_CUSTOMER
    )
    @Parameter(
        name = "medicalArea",
        description = """Normally, the `medicalArea` parameter should not be used,
         and the triage will include that choice in the questionnaire. The `medicalArea` parameter can be used
          when a user has stopped an existing triage before selecting a medical area, 
          then after chatting with an employee, wants to pick up the triage on the specified medical area."""
    )
    @Parameter(
        name = "lang",
        description = """Triage is only supported in Dutch. Triage is started, if this parameter 
        is omitted or has value of 'nl'. Any other string passed will make triage finish automatically."""
    )
    fun startTriage(
        @RequestParam medicalArea: String?,
        @RequestParam(defaultValue = "nl") lang: Locale
    ): ResponseEntity<TriageStateQuestionResponse> {
        verifyRole(UserRole.CUSTOMER)

        var user = queryAuthenticatedUser()
        when {
            triageService.preferBirthdateOverAge(user) -> user = userService.syncUserBirthdateIfNotExist(user)
            user.age == null -> {
                log.warn("User with uuid=${user.publicId} has no age, but it it required for label with id=${user.labelId}")
                throw createResponseStatusException(HttpStatus.BAD_REQUEST) {
                    "User age is required to proceed with triage, but it is not present. Update user's age"
                }
            }
        }

        if (!LocaleUtils.isAvailableLocale(lang)) {
            throw createResponseStatusException { "Lang is not a valid locale" }
        }

        return ResponseEntity.ok(triageService.startTriage(user, medicalArea, lang))
    }

    @PostMapping("/continue")
    @Operation(
        summary = "Continue a triage after it had been stopped by the user",
        description = """
            If a nurse wants the user to continue his triage where he left of this method continues the triage and 
            returns the next question.
        """
    )
    fun restartTriage(): ResponseEntity<TriageStateQuestionResponse> {
        verifyRole(UserRole.CUSTOMER)

        val user = queryAuthenticatedUser()
        return ResponseEntity.ok(triageService.continueTriage(user))
    }

    @PostMapping("/answers")
    @Operation(
        summary = "Post an answer to a question and get next question if applicable",
        description =
        """
This endpoint is used to send answer to a question. Answers have different types, thus answer object should contain type.

For answers that contain a description with images (type: 'images'), either a description or a list of images
is required. Sending both a description and a list of images is also allowed.
 
 ${Swagger.PERMISSION_CUSTOMER}
"""
    )
    fun nextQuestion(
        @RequestBody answerRequest: AnswerRequest,
        @Schema(
            description = """This flag is required to be set to true in order for backend to not end triage when high 
urgency answer is given. Instead of ending it will allow to answer next question, to end triage stop endpoint has to be called.

IMPORTANT: sending `supportContinuation=false` is deprecated and we will remove support for it in the future
        """
        )
        @RequestParam(defaultValue = "false") supportsContinuation: Boolean
    ): ResponseEntity<AnswerResponse> {
        verifyRole(UserRole.CUSTOMER)

        return ResponseEntity.ok(
            triageService.saveAnswer(
                queryAuthenticatedUser(),
                answerRequest,
                supportsContinuation
            )
        )
    }

    @PostMapping("/questions/previous/{questionId}")
    @Operation(
        summary = "Get previously answered question along with given answer",
        description = Swagger.PERMISSION_CUSTOMER
    )
    fun previousQuestion(
        @PathVariable questionId: String,
        @Schema(
            description = """This flag is required to be set to true in order for backend to not end triage when high 
urgency answer is given. Instead of ending it will allow to answer next question, to end triage stop endpoint has to be called.

IMPORTANT: sending `supportContinuation=false` is deprecated and we will remove support for it in the future
        """
        )
        @RequestParam(defaultValue = "false") supportsContinuation: Boolean
    ): ResponseEntity<AnswerResponse> {
        verifyRole(UserRole.CUSTOMER)

        return ResponseEntity.ok(
            triageService.getQuestionBefore(
                queryAuthenticatedUser(),
                questionId,
                supportsContinuation
            )
        )
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop triage for user", description = Swagger.PERMISSION_CUSTOMER)
    fun stopTriage(@RequestBody stopTriageRequest: StopTriageRequest): ResponseEntity<Void> {
        verifyRole(UserRole.CUSTOMER)

        triageService.stopTriage(queryAuthenticatedUser(), stopTriageRequest)

        return ResponseEntity.ok().build()
    }

    @PostMapping("/image")
    @Operation(
        summary = "Post an image to be used as an answer",
        description =
        """
This endpoint is used to upload an image for future use in a triage answer. The response contains an
image id that can be used in the answer for a question that accepts images.

Images can be uploaded but not used for a triage answer; in that case, the files are cleaned up when the
triage is finished.

Use to `Content-Type` header to specify the image mime type (`image/xxx`). The body of this request 
should contain the binary image files.
 
 ${Swagger.PERMISSION_CUSTOMER}
"""
    )
    fun addImage(
        request: HttpServletRequest,
        @RequestHeader("Content-Type") contentType: String
    ): ResponseEntity<ImageUploadResponse> {
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

        return ResponseEntity.ok(
            triageService.uploadImage(
                queryAuthenticatedUser(),
                contentType,
                ByteArrayInputStream(imageBytes)
            )
        )
    }

    @GetMapping("/image/{id}")
    @Operation(
        summary = "Download an image previously uploaded to the triage",
        description = """
Download an image that was previously uploaded for the ongoing triage. The response is
the raw binary image.

${Swagger.PERMISSION_CUSTOMER}
"""
    )
    fun downloadImage(@PathVariable("id") imageId: String): ResponseEntity<StreamingResponseBody> {
        verifyRole(UserRole.CUSTOMER)

        val image = triageService.downloadImage(queryAuthenticatedUser(), imageId)
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(image.contentType))
            .copyStream(image.stream)
    }
}
