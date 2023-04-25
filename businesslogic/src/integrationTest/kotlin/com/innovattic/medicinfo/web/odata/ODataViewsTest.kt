package com.innovattic.medicinfo.web.odata

import com.innovattic.medicinfo.database.dao.ODataDao
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.test.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.temporal.ChronoUnit

class ODataViewsTest : BaseIntegrationTest() {

    @Autowired
    lateinit var oDataDao: ODataDao

    @Test
    fun `Get customer view works for customer`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val privacyStatement = privacyStatementDao.getExistingOrCreate(customer.id, "v1")
        conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        val oDataCustomer = oDataDao.getCustomerView(customer.publicId.toString())!!
        assertEquals(customer.publicId.toString(), oDataCustomer.customerId)
        assertEquals(customer.created, oDataCustomer.registeredAt)
        assertEquals(customer.age, oDataCustomer.age)
        assertEquals(customer.gender.name.lowercase(), oDataCustomer.gender)
        assertEquals(customer.isInsured, oDataCustomer.isInsured)
        assertEquals(label.code, oDataCustomer.labelCode)
        assertEquals(privacyStatement.version, oDataCustomer.privacyVersion)
        assertEquals(privacyStatement.acceptedAt, oDataCustomer.privacyVersionAcceptedAt)
    }

    @Test
    fun `Get customer view fails for employee`() {
        val employee = createEmployee("e1")
        val oDataCustomer = oDataDao.getCustomerView(employee.publicId.toString())

        assertNull(oDataCustomer)
    }

    @Test
    fun `Get customer message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)

        val messageOne = messageDao.create(customer.id, conversation.id, "Hi from Customer", null, null)

        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.id, messageOne.publicId.toString())
            assertEquals(this.conversationId, conversation.publicId.toString())
            assertEquals(this.senderId, customer.publicId.toString())
            assertEquals(this.senderIsCustomer, true)
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
            assertEquals(this.sendAt, messageOne.created)
            assertEquals(this.isSystemMessage, false)
            assertEquals(this.message, messageOne.message)
        }
    }

    @Test
    fun `Get admin message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val admin = createAdmin("a1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)

        val messageOne = messageDao.create(admin.id, conversation.id, "Hi from Admin", null, null)

        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.id, messageOne.publicId.toString())
            assertEquals(this.conversationId, conversation.publicId.toString())
            assertEquals(this.senderId, admin.publicId.toString())
            assertEquals(this.senderIsCustomer, false)
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
            assertEquals(this.sendAt, messageOne.created)
            assertEquals(this.isSystemMessage, true)
            assertEquals(this.message, messageOne.message)
        }
    }

    @Test
    fun `Get employee message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)

        val messageOne = messageDao.create(employee.id, conversation.id, "Hi from Employee", null, null)

        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.id, messageOne.publicId.toString())
            assertEquals(this.conversationId, conversation.publicId.toString())
            assertEquals(this.senderId, employee.publicId.toString())
            assertEquals(this.senderIsCustomer, false)
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
            assertEquals(this.sendAt, messageOne.created)
            assertEquals(this.isSystemMessage, false)
            assertEquals(this.message, messageOne.message)
        }
    }

    @Test
    fun `Received customer message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        val messageOne = messageDao.create(customer.id, conversation.id, "Hi from Customer", null, null)

        conversationService.received(conversation.publicId, customer)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }

        conversationService.received(conversation.publicId, employee)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, true)
            assertEquals(this.readByReceiver, false)
        }
    }

    @Test
    fun `Read customer message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        val messageOne = messageDao.create(customer.id, conversation.id, "Hi from Customer", null, null)

        conversationService.read(conversation.publicId, customer)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }

        conversationService.read(conversation.publicId, employee)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, true)
        }
    }

    @Test
    fun `Received employee message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        val messageOne = messageDao.create(employee.id, conversation.id, "Hi from Employee", null, null)

        conversationService.received(conversation.publicId, employee)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }

        conversationService.received(conversation.publicId, customer)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, true)
            assertEquals(this.readByReceiver, false)
        }
    }

    @Test
    fun `Read employee message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        val messageOne = messageDao.create(employee.id, conversation.id, "Hi from Employee", null, null)

        conversationService.read(conversation.publicId, employee)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }

        conversationService.read(conversation.publicId, customer)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, true)
        }
    }

    @Test
    fun `Not read employee message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        conversationService.read(conversation.publicId, customer)
        clock.plusTime(5L, ChronoUnit.MINUTES)
        val messageOne = messageDao.create(employee.id, conversation.id, "Hi from Employee", null, null)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }
    }

    @Test
    fun `Not read customer message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        conversationService.read(conversation.publicId, employee)
        clock.plusTime(5L, ChronoUnit.MINUTES)
        val messageOne = messageDao.create(customer.id, conversation.id, "Hi from Customer", null, null)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }
    }

    @Test
    fun `Not received employee message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        conversationService.received(conversation.publicId, customer)
        clock.plusTime(5L, ChronoUnit.MINUTES)
        val messageOne = messageDao.create(employee.id, conversation.id, "Hi from Employee", null, null)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }
    }

    @Test
    fun `Not received customer message view works`() {
        val label = createLabel()
        val customer = createCustomer(label, "c1")
        val employee = createEmployee("e1")
        val conversation = conversationDao.create(customer.id, label.id, ConversationStatus.OPEN)
        conversationService.received(conversation.publicId, employee)
        clock.plusTime(5L, ChronoUnit.MINUTES)
        val messageOne = messageDao.create(customer.id, conversation.id, "Hi from Customer", null, null)
        with(oDataDao.getMessageView(messageOne.publicId.toString())!!) {
            assertEquals(this.receivedByReceiver, false)
            assertEquals(this.readByReceiver, false)
        }
    }
}
