package com.innovattic.medicinfo.logic.dto.salesforce

import java.util.*

data class SalesforceTriageAnswersRequestDto(
    val chatId: UUID,
    val customerId: UUID,
    val labelCode: String,
    val subject: String,
    val question: String,
    val category: String,
    val triageAnswers: List<SalesforceTriageAnswer>,
    val triageAdditionalAnswers: List<SalesforceTriageAnswer>?,
    val selfTriage: Boolean,
    val triageStopped: Boolean,
    val urgency: String,
    val triagePerson: SalesforceTriagePerson?
)

/**
 * Object containing triage answers. Format accepted by Salesforce.
 *
 * @param question full question text
 * @param shortQuestion short question text
 * @param chosenAnswer answers chosen by the user separated
 * by separator [com.innovattic.medicinfo.logic.dto.salesforce.SALESFORCE_ANSWER_SEPARATOR]
 * @param possibleAnswers all possible answers for question
 * separated by separator [com.innovattic.medicinfo.logic.dto.salesforce.SALESFORCE_ANSWER_SEPARATOR]
 */
data class SalesforceTriageAnswer(
    val question: String,
    val questionType: String,
    val shortQuestion: String,
    val chosenAnswer: String,
    val possibleAnswers: String
)

data class SalesforceTriageAttachmentData(
    val userId: UUID,
    val conversationId: UUID,
    val attachments: List<SalesforceTriageAttachment>
)

data class SalesforceTriageAttachment(
    val questionId: String,
    val imageIds: List<String>
)

data class SalesforceTriagePerson(
    var firstname: String? = null,
    var lastname: String? = null,
    var gender: String? = null,
    var relation: String? = null,
    var bsn: String? = null,
    var birthdate: String? = null
)

const val SALESFORCE_ANSWER_SEPARATOR = "|"
