package com.innovattic.medicinfo.test

import com.innovattic.common.file.FileService
import com.innovattic.common.test.TestClock
import com.innovattic.medicinfo.database.dao.AppSelfTestDao
import com.innovattic.medicinfo.database.dao.CalendlyAppointmentDao
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.MessageAttachmentDao
import com.innovattic.medicinfo.database.dao.MessageDao
import com.innovattic.medicinfo.database.dao.PrivacyStatementDao
import com.innovattic.medicinfo.database.dao.TriageAnswerDao
import com.innovattic.medicinfo.database.dao.TriageReportingDao
import com.innovattic.medicinfo.database.dao.TriageStatusDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.AdminDto
import com.innovattic.medicinfo.database.dto.AppointmentType
import com.innovattic.medicinfo.database.dto.CustomerDto
import com.innovattic.medicinfo.database.dto.EmployeeDto
import com.innovattic.medicinfo.database.dto.Gender
import com.innovattic.medicinfo.database.dto.LabelDto
import com.innovattic.medicinfo.dbschema.tables.pojos.CalendlyAppointment
import com.innovattic.medicinfo.dbschema.tables.pojos.Label
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.AppointmentService
import com.innovattic.medicinfo.logic.ConversationService
import com.innovattic.medicinfo.logic.MessageService
import com.innovattic.medicinfo.logic.SelfTestService
import com.innovattic.medicinfo.logic.TriageImageService
import com.innovattic.medicinfo.logic.TriageService
import com.innovattic.medicinfo.logic.TriageUserProfileService
import com.innovattic.medicinfo.logic.UserSelfTestResultService
import com.innovattic.medicinfo.logic.UserService
import com.innovattic.medicinfo.logic.eloqua.EloquaApiClient
import com.innovattic.medicinfo.logic.localazy.LocalazyService
import com.innovattic.medicinfo.logic.salesforce.SalesforceClient
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import com.innovattic.medicinfo.logic.triage.MedicinfoServiceHoursProperties
import com.innovattic.medicinfo.logic.triage.TriageSalesforceService
import com.innovattic.medicinfo.logic.triage.tree.QuestionSchemaService
import com.innovattic.medicinfo.web.security.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test", "test.local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [TestConfig::class])
abstract class BaseIntegrationTest {
    @Autowired lateinit var labelDao: LabelDao
    @Autowired lateinit var userDao: UserDao
    @Autowired lateinit var calendlyAppointmentDao: CalendlyAppointmentDao
    @Autowired lateinit var userService: UserService
    @Autowired lateinit var authenticationService: AuthenticationService
    @Autowired lateinit var conversationService: ConversationService
    @Autowired lateinit var messageService: MessageService
    @Autowired lateinit var conversationDao: ConversationDao
    @Autowired lateinit var messageDao: MessageDao
    @Autowired lateinit var appSelfTestDao: AppSelfTestDao
    @Autowired lateinit var selfTestService: SelfTestService
    @Autowired lateinit var userSelfTestResultService: UserSelfTestResultService
    @Autowired lateinit var messageAttachmentDao: MessageAttachmentDao
    @Autowired lateinit var appointmentService: AppointmentService
    @SpyBean lateinit var salesforceService: SalesforceService
    @Autowired lateinit var salesforceClient: SalesforceClient
    @Autowired lateinit var eloquaApiClients: Map<String, EloquaApiClient>
    @Autowired lateinit var fileService: FileService
    @Autowired lateinit var triageService: TriageService
    @Autowired lateinit var triageImageService: TriageImageService
    @Autowired lateinit var triageStatusDao: TriageStatusDao
    @Autowired lateinit var triageAnswerDao: TriageAnswerDao
    @SpyBean lateinit var questionSchemaService: QuestionSchemaService
    @Autowired lateinit var triageSalesforceService: TriageSalesforceService
    @Autowired lateinit var triageReportingDao: TriageReportingDao
    @Autowired lateinit var triageUserProfileService: TriageUserProfileService
    @Autowired lateinit var privacyStatementDao: PrivacyStatementDao
    @Autowired lateinit var medicinfoServiceHoursProperties: MedicinfoServiceHoursProperties
    @Autowired lateinit var clock: TestClock
    @SpyBean lateinit var localazyService: LocalazyService

    fun createLabel(withPushNotifications: Boolean = false, labelCode: String? = null): Label {
        val identifier = labelCode ?: "integrationtest-" + UUID.randomUUID().toString()
        val label = labelDao.create(LabelDto(code = identifier, name = "Label $identifier"))
        if (!withPushNotifications) return label
        labelDao.registerApiKey(label.id, "test-api-key", "test-arn")
        return labelDao.getById(label.id)!!
    }

    fun getOrCreateLabel(labelCode: String): Label {
        labelDao.getByCode(labelCode)?.let { return it }
        return createLabel(labelCode = labelCode)
    }

    fun createAdmin(name: String) = userDao.createAdmin(AdminDto(displayName = name, email = "$name@test.innovattic"))
    fun createEmployee(name: String) = userDao.createEmployee(EmployeeDto(displayName = name))
    fun createCustomer(
        label: Label,
        name: String,
        gender: Gender = Gender.FEMALE,
        age: Int? = 42,
        insured: Boolean = false,
        configurePushNotifications: Boolean = false
    ): User {
        val user = userDao.createCustomer(label.id, customerDto(label.publicId, name, gender, age, insured))
        if (configurePushNotifications) {
            userDao.registerDeviceToken(user.id, "test-token", "test-arn")
        }
        return user
    }

    fun createCalendlyAppointment(
        user: User,
        salesforceAppointmentId: String = "appointmentId",
        salesforceEventId: String = "eventId",
        appointmentType: AppointmentType = AppointmentType.REGULAR,
        failReason: String? = null
    ): CalendlyAppointment {
        val appointment = calendlyAppointmentDao.create(
            UUID.randomUUID().toString(), user.id, "", "",
            LocalDateTime.now(), LocalDateTime.now(), appointmentType
        )

        if (failReason != null) {
            return calendlyAppointmentDao.updateSalesforceFailReason(appointment.id, failReason)
        }
        return calendlyAppointmentDao.updateSalesforce(
            appointment.id,
            salesforceAppointmentId,
            salesforceEventId,
            "https://video-url.com"
        )
    }

    fun customerDto(labelId: UUID, name: String, gender: Gender = Gender.FEMALE, age: Int? = 42, insured: Boolean = false) =
        CustomerDto(null, null, name, labelId, "$name@test.innovattic", gender, age, insured)

    fun apiKey(user: User) = userDao.generateApiKey(user.id)
}
