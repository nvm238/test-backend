package com.innovattic.medicinfo.logic.triage

import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.logic.TriageCombinedData
import com.innovattic.medicinfo.logic.TriageImageService
import com.innovattic.medicinfo.logic.dto.salesforce.SALESFORCE_ANSWER_SEPARATOR
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAnswer
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAnswersRequestDto
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAttachment
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriageAttachmentData
import com.innovattic.medicinfo.logic.dto.salesforce.SalesforceTriagePerson
import com.innovattic.medicinfo.logic.salesforce.SalesforceService
import com.innovattic.medicinfo.logic.triage.model.ADDITIONAL_INFO_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.BIRTHDAY_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.BSN_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.DescriptiveWithImages
import com.innovattic.medicinfo.logic.triage.model.FIRSTNAME_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.GENDER_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.LASTNAME_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.OTHER_ADDITIONAL_INFO_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.OTHER_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.RELATION_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.StoredAnswer
import com.innovattic.medicinfo.logic.triage.model.getGenderFromAnswer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import javax.annotation.PreDestroy

const val CATEGORY_OTHER_COMPLAINT = "Mijn klacht staat er niet tussen"
const val UNKNOWN_URGENCY = "unknown"

@Service
class TriageSalesforceService(
    private val salesforceService: SalesforceService,
    private val triageImageService: TriageImageService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val pool = Executors.newFixedThreadPool(1)

    @PreDestroy
    fun cleanup() {
        pool.shutdown()
    }

    fun sendTriageAnswers(triageData: TriageCombinedData, questionnaireIsOther: Boolean = false) {
        val salesforceAnswer = assembleAnswer(triageData, questionnaireIsOther)
        salesforceService.sendTriageAnswers(salesforceAnswer)

        val questionIdWithImages = triageData.answers.map { StoredAnswer.ofJson(it.answer.data()) }
            .filterIsInstance<DescriptiveWithImages>()
            .map { SalesforceTriageAttachment(it.questionId, it.imageIds) }
            .takeUnless { it.isEmpty() }
            ?: return

        val userDataWithImages =
            SalesforceTriageAttachmentData(salesforceAnswer.customerId, salesforceAnswer.chatId, questionIdWithImages)
        log.debug("Adding ${questionIdWithImages.size} images to Salesforce case. ConversationId=${triageData.conversation.publicId}")
        salesforceService.addImagesToTriageAsync(userDataWithImages) { imageId ->
            triageImageService.download(
                salesforceAnswer.labelCode,
                triageData.triageStatus.id,
                imageId
            )
        }
    }

    private fun assembleAnswer(data: TriageCombinedData, questionnaireIsOther: Boolean = false): SalesforceTriageAnswersRequestDto {
        val allAnswers = mutableListOf<SalesforceTriageAnswer>()
        val allDivergentAnswers = mutableListOf<SalesforceTriageAnswer>()

        var additionalQuestionAnswer = ""
        for (triageAnswer in data.answers) {
            val questionDefinition =
                data.questionnaireModel.getQuestionByUniqueId(
                    data.userProfile.currentQuestionnaireName,
                    triageAnswer.questionId
                )
            val allPossibleAnswers = questionDefinition.answer
                .filter { it.answerText.isNotEmpty() }
                .joinToString(SALESFORCE_ANSWER_SEPARATOR) { it.answerText }
            val resolvedAnswers = data.getResolvedAnswers(triageAnswer)
            val separatedAnswers = resolvedAnswers.joinToString(SALESFORCE_ANSWER_SEPARATOR) { it.answerText }
            val salesforceAnswer = SalesforceTriageAnswer(
                question = questionDefinition.getQuestionTextFor(data.userProfile),
                questionType = questionDefinition.questionType.name,
                shortQuestion = questionDefinition.shortQuestionVpk,
                chosenAnswer = separatedAnswers,
                possibleAnswers = allPossibleAnswers
            )
            allAnswers.add(salesforceAnswer)

            val divergentAnswers = resolvedAnswers.filter { it.isDivergent }
            if (divergentAnswers.isNotEmpty()) {
                val joinedAnswers = divergentAnswers.joinToString(SALESFORCE_ANSWER_SEPARATOR) { it.answerText }
                val divergentAnswer = SalesforceTriageAnswer(
                    question = questionDefinition.getQuestionTextFor(data.userProfile),
                    questionType = questionDefinition.questionType.name,
                    shortQuestion = questionDefinition.shortQuestionVpk,
                    chosenAnswer = joinedAnswers,
                    possibleAnswers = allPossibleAnswers
                )
                allDivergentAnswers.add(divergentAnswer)
            }

            if (
                triageAnswer.questionId == ADDITIONAL_INFO_QUESTION_UNIQUE_ID ||
                triageAnswer.questionId == OTHER_ADDITIONAL_INFO_QUESTION_UNIQUE_ID
            ) {
                if (additionalQuestionAnswer.isNotBlank()) {
                    additionalQuestionAnswer += " /n "
                }
                additionalQuestionAnswer += salesforceAnswer.chosenAnswer
            }
        }

        val urgency = getUrgencyString(data)

        return SalesforceTriageAnswersRequestDto(
            chatId = data.conversation.publicId,
            customerId = data.user.publicId,
            labelCode = data.label.code,
            subject = selectCategory(data, questionnaireIsOther),
            question = additionalQuestionAnswer,
            category = selectCategory(data, questionnaireIsOther),
            triageAnswers = allAnswers,
            triageAdditionalAnswers = allDivergentAnswers,
            selfTriage = data.userProfile.isAppUser,
            triageStopped = data.triageStatus.status.isStopped,
            urgency = urgency,
            triagePerson = if (data.userProfile.isAppUser) null else assemblePersonInfoFromAnswers(data)
        )
    }

    private fun getUrgencyString(data: TriageCombinedData): String {
        // If no answers have been given all possible answers (including the high urgency ones) are applicable
        val lastAnswer = data.answers.lastOrNull() ?: return UNKNOWN_URGENCY

        val lastQuestionDefinition = data.questionnaireModel.getQuestionByUniqueId(
            data.userProfile.currentQuestionnaireName,
            lastAnswer.questionId
        )

        if (lastQuestionDefinition.isProfileQuestion() ||
            lastQuestionDefinition.isMedicalAreaQuestion() ||
            lastQuestionDefinition.questionnaireId == OTHER_QUESTIONNAIRE_ID
        ) {
            return UNKNOWN_URGENCY
        }

        var possibleQuestion = lastQuestionDefinition
        // Go through all the possible questions and retrieve the possible answers if one of the possible answers is
        // high urgency return unknown.
        while (true) {
            possibleQuestion = data.questionnaireModel.getNextAskableQuestion(possibleQuestion, data.userProfile) ?: break
            if (possibleQuestion.answer.any { it.action.urgency in 1..2 }) return UNKNOWN_URGENCY
        }

        // No high urgency questions were found. Return the urgency as we always did.
        return data.determineUrgencyFromAnswersString()?.urgencyName ?: ""
    }

    private fun getMedAreaTranslation(data: TriageCombinedData) =
        data.questionnaireModel.getQuestionnaire(data.userProfile.currentQuestionnaireName)?.displayName ?: ""

    private fun selectCategory(data: TriageCombinedData, questionnaireIsOther: Boolean = false) =
        if (
            data.triageStatus.status == TriageProgress.FINISHED_BY_USER_NO_MEDAREA ||
            questionnaireIsOther
        ) {
            CATEGORY_OTHER_COMPLAINT
        } else if (data.userProfile.currentQuestionnaireName == PROFILE_QUESTIONNAIRE_ID) {
            ""
        } else getMedAreaTranslation(data)

    private fun assemblePersonInfoFromAnswers(triageData: TriageCombinedData): SalesforceTriagePerson {
        val triagePerson = SalesforceTriagePerson()
        triageData.answers.forEach { answer ->
            val resolvedAnswers = triageData.getResolvedAnswers(answer)
            when (answer.questionId) {
                RELATION_QUESTION_UNIQUE_ID -> {
                    triagePerson.relation = resolvedAnswers.first().answerText
                }
                FIRSTNAME_QUESTION_UNIQUE_ID -> {
                    triagePerson.firstname = resolvedAnswers.first().answerText
                }
                LASTNAME_QUESTION_UNIQUE_ID -> {
                    triagePerson.lastname = resolvedAnswers.first().answerText
                }
                GENDER_QUESTION_UNIQUE_ID -> {
                    val genderAnswer = StoredAnswer.ofJson(answer.answer.data())
                    triagePerson.gender = getGenderFromAnswer(genderAnswer).name.lowercase()
                }
                BIRTHDAY_QUESTION_UNIQUE_ID -> {
                    triagePerson.birthdate = resolvedAnswers.first().answerText
                }
                BSN_QUESTION_UNIQUE_ID -> {
                    triagePerson.bsn = resolvedAnswers.first().answerText
                }
            }
        }

        return triagePerson
    }
}
