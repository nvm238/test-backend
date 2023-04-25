package com.innovattic.medicinfo.web.endpoint.v1

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.file.fileResponseBuilder
import com.innovattic.common.file.streamBody
import com.innovattic.medicinfo.database.dto.ActionType.Companion.ACTION_TYPE_CONTEXT_DESCRIPTION_DOC
import com.innovattic.medicinfo.database.dto.ConversationDto
import com.innovattic.medicinfo.database.dto.MessageDto
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.file.ImageFormatDetection
import com.innovattic.medicinfo.logic.ConversationService
import com.innovattic.medicinfo.logic.ImageService
import com.innovattic.medicinfo.logic.MessageService
import com.innovattic.medicinfo.logic.OnlineEmployeeService
import com.innovattic.medicinfo.logic.dto.CreateMessageDto
import com.innovattic.medicinfo.logic.dto.OnlineEmployeeDto
import com.innovattic.medicinfo.logic.dto.UpdateMessageDto
import com.innovattic.medicinfo.web.endpoint.Swagger
import com.innovattic.medicinfo.web.security.verifyRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.security.Principal
import java.time.Duration
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("v1/conversation")
class ConversationEndpoint(
    private val conversationService: ConversationService,
    private val messageService: MessageService,
    private val imageService: ImageService,
    private val onlineEmployeeService: OnlineEmployeeService
) : BaseEndpoint() {

    @MessageMapping("/conversation/{id}/typing/start")
    fun startTyping(@AuthenticationPrincipal principal: Principal, @DestinationVariable("id") id: UUID) {
        val user = queryAuthenticatedUser(principal)
        conversationService.startTyping(id, user)
    }

    @MessageMapping("/conversation/{id}/typing/stop")
    fun stopTyping(@AuthenticationPrincipal principal: Principal, @DestinationVariable("id") id: UUID) {
        val user = queryAuthenticatedUser(principal)
        conversationService.stopTyping(id, user)
    }

    @PostMapping
    @Operation(summary = "Create conversation", description = Swagger.PERMISSION_CUSTOMER)
    @Deprecated("Conversations are to be created through starting a triage")
    fun create(): ConversationDto {
        verifyRole(UserRole.CUSTOMER)

        return conversationService.create(queryAuthenticatedUser())
    }

    @PostMapping("{id}/received")
    @Operation(summary = "Mark conversation as received.", description = Swagger.PERMISSION_ALL)
    fun received(@PathVariable("id") id: UUID) {
        verifyRole(UserRole.CUSTOMER, UserRole.EMPLOYEE, UserRole.ADMIN)

        return conversationService.received(id, queryAuthenticatedUser())
    }

    @PostMapping("{id}/read")
    @Operation(summary = "Mark conversation as read", description = Swagger.PERMISSION_ALL)
    fun read(@PathVariable("id") id: UUID) {
        verifyRole(UserRole.CUSTOMER, UserRole.EMPLOYEE, UserRole.ADMIN)

        return conversationService.read(id, queryAuthenticatedUser())
    }

    @PostMapping("{id}/archive")
    @Operation(
        summary = "Archive conversation by id",
        description = Swagger.PERMISSION_CUSTOMER + " - Can only archive their own conversation<br/>" +
            Swagger.PERMISSION_ADMIN + " - Can archive all conversations"
    )
    fun archive(@PathVariable("id") id: UUID) {
        verifyRole(UserRole.CUSTOMER, UserRole.ADMIN)

        return conversationService.archive(id, queryAuthenticatedUser())
    }

    @PostMapping("/{id}/message")
    @Operation(
        summary = "Create a message in a conversation",
        description = ACTION_TYPE_CONTEXT_DESCRIPTION_DOC + Swagger.PERMISSION_CUSTOMER
    )
    fun createMessage(
        @PathVariable("id") id: UUID,
        @Valid @RequestBody dto: CreateMessageDto,
    ): MessageDto {
        verifyRole(UserRole.CUSTOMER, UserRole.EMPLOYEE, UserRole.ADMIN)
        val user = queryAuthenticatedUser()

        return messageService.create(user, id, dto)
    }

    @PostMapping("/{id}/image")
    @Operation(
        summary = "Create a message with image in a conversation",
        description =
        """See 'MedicInfo API documentation' for usage and examples.<br/><br/>""" + Swagger.PERMISSION_CUSTOMER
    )
    fun createMessage(
        @PathVariable("id") id: UUID,
        @Schema(description = "Message text to send. Emoticons are supported.")
        @RequestPart("message", required = false) message: String?,
        @Schema(description = "Attachment file to send. Supported types: PNG, JPEG, JPG.")
        @RequestPart("attachment") file: MultipartFile
    ): MessageDto {
        verifyRole(UserRole.CUSTOMER, UserRole.EMPLOYEE, UserRole.ADMIN)
        val user = queryAuthenticatedUser()

        val name = file.originalFilename ?: throw createResponseStatusException { "Must specify filename" }
        val type = file.contentType ?: throw createResponseStatusException { "Must specify content type" }
        // MultipartFile returns new stream everytime getInputStream is called, caller is responsible to close it
        file.inputStream.use {
            if (!ImageFormatDetection.isSupportedFormat(it)) {
                throw createResponseStatusException { "Image format not supported" }
            }
        }

        return file.inputStream.use {
            messageService.createWithAttachment(user, id, name, type, it, message)
        }
    }

    @GetMapping("/{conversationId}/image/{imageId}")
    @Operation(summary = "Get an image in a conversation", description = Swagger.PERMISSION_ALL)
    fun getMessageImage(
        @Schema(description = "Conversation id of where the image is located")
        @PathVariable("conversationId") conversationId: UUID,
        @Schema(description = "Id of the image")
        @PathVariable("imageId") imageId: UUID
    ): ResponseEntity<StreamingResponseBody> {
        val (imageUUID, mediaType, inputStream) =
            imageService.getImage(conversationId, imageId, queryAuthenticatedUser())

        return fileResponseBuilder(imageUUID.toString() + "." + mediaType.subtype, mediaType)
            .cacheControl(CacheControl.maxAge(Duration.ofDays(365)))
            // TODO: use innolib version when available (https://gitlab.innovattic.com/innovattic/innolib-backend/-/issues/4)
            .streamBody { out ->
                inputStream.use {
                    it.copyTo(out)
                }
            }
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get conversations and messages in a conversation",
        description = ACTION_TYPE_CONTEXT_DESCRIPTION_DOC + Swagger.PERMISSION_ALL
    )
    fun get(
        @PathVariable("id") id: UUID,
        @Parameter(
            description = Swagger.FIQL + "Supported fields: created from the ConversationDto object",
            example = Swagger.FIQL_EXAMPLE
        )
        @RequestParam("query") query: String?,
        @RequestParam("order") order: List<String>?
    ): ConversationDto {
        verifyRole(UserRole.CUSTOMER, UserRole.EMPLOYEE, UserRole.ADMIN)

        return messageService.get(queryAuthenticatedUser(), id, query, order)
    }

    @GetMapping("online-employees")
    @Operation(summary = "Get number of online employees", description = Swagger.PERMISSION_ALL)
    fun getOnlineEmployees(): ResponseEntity<OnlineEmployeeDto> {
        verifyRole(UserRole.CUSTOMER, UserRole.EMPLOYEE, UserRole.ADMIN)
        val dto = OnlineEmployeeDto(onlineEmployeeService.latestValue)
        return ResponseEntity.ok(dto)
    }

    @PostMapping("/{conversationId}/message/{messageId}")
    @Operation(
        summary = """Update the message. For now, only the translation can be set for an existing message. 
Only used internally - do not call from apps""",
        description = "${Swagger.PERMISSION_EMPLOYEE}, ${Swagger.PERMISSION_ADMIN}"
    )
    fun updateMessage(
        @Schema(description = "Conversation id which contains message to add translation to")
        @PathVariable("conversationId") conversationId: UUID,
        @Schema(description = "UUID of the message to add translation to")
        @PathVariable("messageId") messageId: UUID,
        @RequestBody updateMessageDto: UpdateMessageDto
    ): ResponseEntity<Unit> {
        verifyRole(UserRole.EMPLOYEE, UserRole.ADMIN)

        return ResponseEntity.ok(
            messageService.addTranslation(
                queryAuthenticatedUser(),
                conversationId,
                messageId,
                updateMessageDto.translatedMessage
            )
        )
    }
}
