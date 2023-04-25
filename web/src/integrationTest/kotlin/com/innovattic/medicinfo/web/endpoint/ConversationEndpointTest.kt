package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.database.databaseNow
import com.innovattic.common.test.assertEmptyResponse
import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertObjectResponse
import com.innovattic.common.test.assertResponse
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dto.ActionDto
import com.innovattic.medicinfo.database.dto.ActionType
import com.innovattic.medicinfo.database.dto.AttachmentType
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.MessageDto
import com.innovattic.medicinfo.database.dto.TranslationCombineUtil
import com.innovattic.medicinfo.logic.dto.CreateMessageDto
import com.innovattic.medicinfo.logic.dto.UpdateMessageDto
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.emptyCollectionOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.temporal.ChronoUnit
import java.util.*

class ConversationEndpointTest : BaseEndpointTest() {

    @Test
    fun createConversation_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation")
        } Then {
            assertObjectResponse()
            body("id", notNullValue())
            body("created", notNullValue())
            body("customer.id", equalTo(user.publicId.toString()))
            body("customer.name", equalTo(user.name))
            body("customer.age", equalTo(user.age))
            body("customer.gender", equalTo(user.gender.name.lowercase()))
            body("status", equalTo(ConversationStatus.OPEN.value))
            body("messages", emptyCollectionOf(MessageDto::class.java))
        }
    }

    @Test
    fun archiveConversation_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation/${conversation.id}/archive")
        } Then {
            assertEmptyResponse()
        }
        assertEquals(ConversationStatus.ARCHIVED, conversationDao.get(conversation.id)?.status)
    }

    @Test
    fun archiveConversation_works_forAdmin() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        Given {
            auth().oauth2(adminAccessToken)
        } When {
            post("v1/conversation/${conversation.id}/archive")
        } Then {
            assertEmptyResponse()
        }
        assertEquals(ConversationStatus.ARCHIVED, conversationDao.get(conversation.id)?.status)
    }

    @Test
    fun archiveConversation_fails_forCustomerWhichIsNotTheCreator() {
        val user = createCustomer(createLabel(), "c1")
        val nonConversationCreator = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        Given {
            auth().oauth2(accessToken(nonConversationCreator))
        } When {
            post("v1/conversation/${conversation.id}/archive")
        } Then {
            statusCode(200)
        }
        assertEquals(ConversationStatus.OPEN, conversationDao.get(conversation.id)?.status)
    }

    @Test
    fun readConversation_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        clock.lockTime()
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation/${conversation.id}/read")
        } Then {
            statusCode(200)
        }

        with(conversationDao.get(conversation.id)!!) {
            assertEquals(databaseNow(clock), readByCustomer)
            assertNull(readByEmployee)
        }
    }

    @Test
    fun readConversation_works_forEmployee() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")
        clock.lockTime()
        Given {
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/read")
        } Then {
            statusCode(200)
        }
        with(conversationDao.get(conversation.id)!!) {
            assertEquals(databaseNow(clock), readByEmployee)
            assertNull(readByCustomer)
        }
    }

    @Test
    fun createMessage_withImageAndMessage_works_forCustomer() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val message = "This is a message"
        Given {
            multiPart("attachment", ClassPathResource("innovattic-logo-dark.png").file, "image/png")
            multiPart("message", message)
            auth().oauth2(accessToken(user))
            contentType("multipart/form-data")
        } When {
            post("v1/conversation/${conversation.id}/image")
        } Then {
            body("userId", equalTo(user.publicId.toString()))
            body("userName", equalTo(user.name))
            body("userRole", equalTo(user.role.name.lowercase()))
            body("created", notNullValue())
            body("message", equalTo(message))
            body("attachment.url", containsString("${conversation.id}/image/"))
            body("attachment.type", equalTo(AttachmentType.IMAGE.name.lowercase()))
        }
    }

    @Test
    fun createMessage_withImage_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        Given {
            multiPart("attachment", ClassPathResource("innovattic-logo-dark.png").file, "image/png")
            auth().oauth2(accessToken(user))
            contentType("multipart/form-data")
        } When {
            post("v1/conversation/${conversation.id}/image")
        } Then {
            body("userId", equalTo(user.publicId.toString()))
            body("userName", equalTo(user.name))
            body("userRole", equalTo(user.role.name.lowercase()))
            body("created", notNullValue())
        }
    }

    @Test
    fun createMessage_forExpiredConversation_fails_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val requestBody = MessageDto(
            message = "This is a message"
        )
        conversationService.archive(conversation.id, createAdmin("admin"))
        Given {
            body(requestBody)
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            assertErrorResponse(ErrorCodes.CONVERSATION_EXPIRED, status = HttpStatus.BAD_REQUEST, errorCodeField = "errorCode")
        }
    }

    @Test
    fun createMessage_forNotExpiredConversation_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val requestBody = CreateMessageDto(
            message = "This is a message"
        )
        clock.lockTime()
        messageService.create(user, conversation.id, requestBody)
        clock.plusTime(24L, ChronoUnit.HOURS)
        messageService.create(user, conversation.id, requestBody)
        clock.plusTime(36L, ChronoUnit.HOURS)
        Given {
            body(requestBody)
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            body("userId", equalTo(user.publicId.toString()))
            body("userName", equalTo(user.name))
            body("userRole", equalTo(user.role.name.lowercase()))
            body("created", notNullValue())
        }
    }

    @Test
    fun createMessage_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val requestBody = MessageDto(
            message = "This is a message"
        )
        Given {
            body(requestBody)
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            body("userId", equalTo(user.publicId.toString()))
            body("userName", equalTo(user.name))
            body("userRole", equalTo(user.role.name.lowercase()))
            body("created", notNullValue())
            body("message", equalTo(requestBody.message))
        }
    }

    @Test
    fun createMessage_fails_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val nonConversationCreator = createCustomer(createLabel(), "c1")
        val requestBody = MessageDto(
            message = "This is a message"
        )
        Given {
            body(requestBody)
            auth().oauth2(accessToken(nonConversationCreator))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun createMessage_works_forEmployee() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN, Locale.ENGLISH)
        val employee = createEmployee("employee")
        val requestBody = MessageDto(
            message = "This is a message by an employee",
            translatedMessage = "Dies ist eine Nachricht eines Mitarbeiters"
        )
        Given {
            body(requestBody)
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.publicId}/message")
        } Then {
            body("userId", equalTo(employee.publicId.toString()))
            body("userName", equalTo(employee.name))
            body("userRole", equalTo(employee.role.name.lowercase()))
            body("created", notNullValue())
            body("message", equalTo(TranslationCombineUtil.combine(requestBody.message!!, requestBody.translatedMessage, "en")))
            body("translatedMessage", equalTo(requestBody.translatedMessage))
        }
    }

    @Test
    fun createMessage_withAction_fails_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val requestBody = MessageDto(
            message = "This is a message",
            action = ActionDto(ActionType.CONFIRM_APPOINTMENT)
        )
        Given {
            body(requestBody)
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            assertErrorResponse(null, HttpStatus.FORBIDDEN)
        }
    }

    @Test
    fun createMessage_withAction_works_forAdmin() {
        val user = createCustomer(createLabel(), "c1")
        val admin = createAdmin("admin")
        val conversation = conversationService.create(user)
        val requestBody = MessageDto(
            message = "This is a message",
            action = ActionDto(ActionType.CONFIRM_APPOINTMENT)
        )
        Given {
            body(requestBody)
            auth().oauth2(accessToken(admin))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            body("userId", equalTo(admin.publicId.toString()))
            body("userName", equalTo(admin.name))
            body("userRole", equalTo(admin.role.name.lowercase()))
            body("created", notNullValue())
            body("message", equalTo(requestBody.message))
            body("action.type", equalTo(ActionType.CONFIRM_APPOINTMENT.name.lowercase()))
        }
    }

    @Test
    fun createMessage_withInvalidActionContext_fails_forEmployee() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")

        Given {
            body(
                """
                     {
                        "message": "This is a message",
                        "action": {
                            "type": "confirm_appointment",
                            "context": "invalid"
                        }
                     }
            """.trimIndent()
            )
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun createMessage_withAction_works_forEmployee() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")
        val now = ZonedDateTime.now(clock)
        val requestBody = MessageDto(
            message = "This is a message by an employee",
            action = ActionDto(
                ActionType.CONFIRM_APPOINTMENT,
                context = mapOf("appointment_id" to 12345, "appointment_date" to now)
            )
        )
        Given {
            body(requestBody)
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            body("userId", equalTo(employee.publicId.toString()))
            body("userName", equalTo(employee.name))
            body("userRole", equalTo(employee.role.name.lowercase()))
            body("created", notNullValue())
            body("message", equalTo(requestBody.message))
            body("action.type", equalTo(ActionType.CONFIRM_APPOINTMENT.name.lowercase()))
            body("action.context.appointment_id", equalTo(12345))
            body("action.context.appointment_date", equalTo(ISO_OFFSET_DATE_TIME.format(now)))
        }
    }

    @Test
    fun createMessage_withVideoChatMessageAction_enrichesAppointmentId() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")
        val salesforceAppointmentId = UUID.randomUUID()
        val appointment = createCalendlyAppointment(user, salesforceAppointmentId.toString(), "salesforceEventId")

        val requestBody = CreateMessageDto(
            message = "This is a video chat message with action context",
            action = ActionDto(
                ActionType.VIDEO_CHAT_MESSAGE,
                context = mapOf("appointmentId" to salesforceAppointmentId.toString())
            )
        )

        Given {
            body(requestBody)
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            body("action.type", equalTo(ActionType.VIDEO_CHAT_MESSAGE.name.lowercase()))
            body("action.context.appointmentId", equalTo(salesforceAppointmentId.toString()))
            body("action.context.appointmentPublicId", equalTo(appointment.publicId.toString()))
        }
    }

    @Test
    fun createMessage_withVideoChatMessageAction_withoutAppointmentIdWorks() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")
        val requestBody = CreateMessageDto(
            message = "This is a video chat message with action context",
            action = ActionDto(
                ActionType.VIDEO_CHAT_MESSAGE,
                context = mapOf("otherContext" to "contextValue")
            )
        )

        Given {
            body(requestBody)
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            body("action.type", equalTo(ActionType.VIDEO_CHAT_MESSAGE.name.lowercase()))
            body("action.context.otherContext", equalTo("contextValue"))
        }
    }

    @Test
    fun createMessage_withVideoChatMessageAction_withUnknownSalesforceAppointmentId_returnsNotFound() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")

        val requestBody = CreateMessageDto(
            message = "This is a video chat message with action context",
            action = ActionDto(
                ActionType.VIDEO_CHAT_MESSAGE,
                context = mapOf("appointmentId" to "12345")
            )
        )

        Given {
            body(requestBody)
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun readImage_works_forCustomer() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val imageMessage = messageService.createWithAttachment(
            user,
            conversation.id,
            "filename",
            "image/png",
            ClassPathResource("innovattic-logo-dark.png").inputStream,
        )
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/${imageMessage.attachment!!.url}")
        } Then {
            assertResponse(status = HttpStatus.OK, contentType = MediaType.IMAGE_PNG)
        }
    }

    @Test
    fun readImage_fails_forCustomer() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val otherUser = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val imageMessage = messageService.createWithAttachment(
            user,
            conversation.id,
            "filename",
            "image/png",
            ClassPathResource("innovattic-logo-dark.png").inputStream
        )
        Given {
            auth().oauth2(accessToken(otherUser))
        } When {
            get("v1/${imageMessage.attachment!!.url}")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun readMessage_works_forCustomer() {
        val label = createLabel()
        val user = createCustomer(label, "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")
        val messages = listOf(
            MessageDto(message = "Message 1 by customer"),
            MessageDto(message = "Message 2 by customer")
        )
        clock.lockTime()
        val now = ZonedDateTime.now(clock)
        messages.forEach {
            messageService.create(user, conversation.id, CreateMessageDto(message = it.message!!))
        }
        messageService.create(
            employee, conversation.id, CreateMessageDto(
                message = "This is a message by an employee",
                action = ActionDto(
                    ActionType.CONFIRM_APPOINTMENT,
                    context = mapOf("appointment_id" to 12345, "appointment_date" to now)
                )
            )
        )
        val imageMessage = messageService.createWithAttachment(
            user,
            conversation.id,
            "filename",
            "image/png",
            ClassPathResource("innovattic-logo-dark.png").inputStream,
            "image"
        )
        Given {
            auth().oauth2(accessToken(user))
            queryParam("query", "created==${ZonedDateTime.now(clock)}")
        } When {
            get("v1/conversation/${conversation.id}")
        } Then {
            assertObjectResponse()
            body("id", notNullValue())
            body("created", notNullValue())
            body("customer.id", equalTo(user.publicId.toString()))
            body("customer.name", equalTo(user.name))
            body("customer.age", equalTo(user.age))
            body("customer.gender", equalTo(user.gender.name.lowercase()))
            body("status", equalTo(ConversationStatus.OPEN.value))
            body("messages[0].message", equalTo(messages.first().message))
            body("messages[0].userName", equalTo(user.name))
            body("messages[0].userRole", equalTo(user.role.name.lowercase()))
            body("messages[0].userId", equalTo(user.publicId.toString()))
            body("messages[1].message", equalTo(messages[1].message))
            body("messages[1].userName", equalTo(user.name))
            body("messages[1].userRole", equalTo(user.role.name.lowercase()))
            body("messages[1].userId", equalTo(user.publicId.toString()))
            body("messages[2].userName", equalTo(employee.name))
            body("messages[2].userRole", equalTo(employee.role.name.lowercase()))
            body("messages[2].userId", equalTo(employee.publicId.toString()))
            body("messages[2].action.type", equalTo(ActionType.CONFIRM_APPOINTMENT.name.lowercase()))
            body("messages[2].action.context.appointment_id", equalTo(12345))
            body("messages[2].action.context.appointment_date", equalTo(ISO_OFFSET_DATE_TIME.format(now)))
            body("messages[3].message", equalTo(imageMessage.message))
            body("messages[3].userName", equalTo(user.name))
            body("messages[3].userRole", equalTo(user.role.name.lowercase()))
            body("messages[3].userId", equalTo(user.publicId.toString()))
            body("messages[3].attachment.url", containsString("${conversation.id}/image/"))
            body("messages[3].attachment.type", equalTo(imageMessage.attachment!!.type.name.lowercase()))
        }
    }

    @Test
    fun readMessageWithQuery_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val message = messageService.create(user, conversation.id, CreateMessageDto(message = "Message 1 by customer"))

        clock.plusTime(10L, ChronoUnit.MINUTES)

        messageService.create(user, conversation.id, CreateMessageDto(message = "Message 2 by customer"))

        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/conversation/${conversation.id}/?query=created=le=${message.created};created=ge=${message.created}")
        } Then {
            assertObjectResponse()
            body("messages[0].message", equalTo(message.message))
            body("messages[0].userName", equalTo(user.name))
            body("messages[0].userId", equalTo(user.publicId.toString()))
            body("messages[0].userRole", equalTo(user.role.name.lowercase()))
            body("messages[1].message", nullValue())
            body("messages[1].userId", nullValue())
        }
    }

    @Test
    fun readMessage_fails_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val nonConversationCreator = createCustomer(createLabel(), "c1")
        messageService.create(user, conversation.id, CreateMessageDto(message = "Message 1 by customer"))
        Given {
            auth().oauth2(accessToken(nonConversationCreator))
        } When {
            get("v1/conversation/${conversation.id}/")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun readMessage_works_forEmployee() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        messageService.create(user, conversation.id, CreateMessageDto(message = "Message 1 by customer"))
        val employee = createEmployee("employee")
        Given {
            auth().oauth2(accessToken(employee))
        } When {
            get("v1/conversation/${conversation.id}/")
        } Then {
            assertObjectResponse()
        }
    }

    @Test
    fun `given conversation with translated message and device lanuguage en, when calling endpoint as customer, expect just nurse messages with translation`() {
        val user = createCustomer(createLabel(), "c1")
        val locale = Locale.forLanguageTag("en")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN, locale)
        val employee = createEmployee("employee")
        val customerMessage = "Message 1 by customer"
        messageService.create(user, conversation.publicId, CreateMessageDto(message = customerMessage, "translation that should not be visible"))
        val employeeMessage = "Message 1 by employee"
        val employeeMessageTranslation = "translation"
        messageService.create(employee, conversation.publicId, CreateMessageDto(message = employeeMessage,
            employeeMessageTranslation
        ))
        Given {
            auth().oauth2(accessToken(user))
        } When {
            get("v1/conversation/${conversation.publicId}/")
        } Then {
            body("messages[0].message", equalTo(customerMessage))
            body("messages[1].message", equalTo(TranslationCombineUtil.combine(employeeMessage, employeeMessageTranslation, locale.language)))
        }
    }

    @Test
    fun `given conversation with translated message and device lanuguage en, when calling endpoint as nurse, expect all translations of all messages visible`() {
        val user = createCustomer(createLabel(), "c1")
        val locale = Locale.forLanguageTag("en")
        val conversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN, locale)
        val employee = createEmployee("employee")
        val customerMessage = "Message 1 by customer"
        val customerMessageTranslation = "translation should be visible"
        messageService.create(user, conversation.publicId, CreateMessageDto(message = customerMessage,
            customerMessageTranslation
        ))
        val employeeMessage = "Message 1 by employee"
        val employeeMessageTranslation = "translation"
        messageService.create(employee, conversation.publicId, CreateMessageDto(message = employeeMessage,
            employeeMessageTranslation
        ))
        Given {
            auth().oauth2(accessToken(employee))
        } When {
            get("v1/conversation/${conversation.publicId}/")
        } Then {
            body("messages[0].message", equalTo(customerMessage))
            body("messages[1].message", equalTo(employeeMessage))
        }
    }

    @Test
    fun receiveConversation_works_forEmployee() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")
        clock.lockTime()
        Given {
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/received")
        } Then {
            assertEmptyResponse()
        }

        with(conversationDao.get(conversation.id)!!) {
            assertNull(deliveredToCustomer)
            assertEquals(databaseNow(clock), deliveredToEmployee)
        }
    }

    @Test
    fun receiveConversation_works_forCustomer() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        clock.lockTime()
        Given {
            auth().oauth2(accessToken(user))
        } When {
            post("v1/conversation/${conversation.id}/received")
        } Then {
            assertEmptyResponse()
        }

        with(conversationDao.get(conversation.id)!!) {
            assertEquals(databaseNow(clock), deliveredToCustomer)
            assertNull(deliveredToEmployee)
        }
    }

    @Test
    fun createMessage_withTriageAction_works() {
        val user = createCustomer(createLabel(), "c1")
        val conversation = conversationService.create(user)
        val employee = createEmployee("employee")
        val requestBody = MessageDto(
            message = "This is a message by an employee",
            action = ActionDto(
                ActionType.TRIAGE,
                context = mapOf("complaintAreaTriage" to "KEELK")
            )
        )
        Given {
            body(requestBody)
            auth().oauth2(accessToken(employee))
        } When {
            post("v1/conversation/${conversation.id}/message")
        } Then {
            body("message", equalTo(requestBody.message))
            body("action.type", equalTo(ActionType.TRIAGE.name.lowercase()))
            body("action.context.complaintAreaTriage", equalTo("KEELK"))
        }
    }

    @Test
    fun addTranslationToMessage() {
        val user = createCustomer(createLabel(), "c1")
        val admin = createAdmin("admin")
        val conversation = conversationService.create(user)
        val message = messageService.create(user, conversation.id, CreateMessageDto(message = "Message 1 by customer"))
        val translationMessage = UpdateMessageDto("Nachricht 1 vom Kunden")
        Given {
            body(translationMessage)
            auth().oauth2(accessToken(admin))
        } When {
            post("v1/conversation/${conversation.id}/message/${message.id}")
        } Then {
            statusCode(200)
        }

        val updatedMessage = messageService.get(user, conversation.id, null, null)
        assertEquals(translationMessage.translatedMessage, updatedMessage.messages[0].translatedMessage)
    }

}
