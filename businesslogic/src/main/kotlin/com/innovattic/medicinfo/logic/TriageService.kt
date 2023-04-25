package com.innovattic.medicinfo.logic

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.error.failResponseIf
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.MessageDao
import com.innovattic.medicinfo.database.dao.TriageAnswerDao
import com.innovattic.medicinfo.database.dao.TriageReportingDao
import com.innovattic.medicinfo.database.dao.TriageStatusDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.database.dto.CustomerEntryType
import com.innovattic.medicinfo.database.dto.TriageProgress
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.dbschema.tables.pojos.Label
import com.innovattic.medicinfo.dbschema.tables.pojos.ReportingTriage
import com.innovattic.medicinfo.dbschema.tables.pojos.ReportingTriageAnswer
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageAnswer
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.salesforce.SALESFORCE_ANSWER_SEPARATOR
import com.innovattic.medicinfo.logic.dto.triage.AnswerRequest
import com.innovattic.medicinfo.logic.dto.triage.AnswerResponse
import com.innovattic.medicinfo.logic.dto.triage.ImageUploadResponse
import com.innovattic.medicinfo.logic.dto.triage.QuestionResponse
import com.innovattic.medicinfo.logic.dto.triage.SingleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.StopReason
import com.innovattic.medicinfo.logic.dto.triage.StopTriageRequest
import com.innovattic.medicinfo.logic.dto.triage.TriageStateQuestionResponse
import com.innovattic.medicinfo.logic.localazy.LocalazyService
import com.innovattic.medicinfo.logic.triage.AnswerValidatorService
import com.innovattic.medicinfo.logic.triage.MedicinfoServiceHoursProperties
import com.innovattic.medicinfo.logic.triage.TriagePreferBirthdateFeatureProperties
import com.innovattic.medicinfo.logic.triage.TriageSalesforceService
import com.innovattic.medicinfo.logic.triage.isContinued
import com.innovattic.medicinfo.logic.triage.isRestartStatus
import com.innovattic.medicinfo.logic.triage.isStarted
import com.innovattic.medicinfo.logic.triage.model.Action
import com.innovattic.medicinfo.logic.triage.model.ActionType.ASK_FOR_CHAT
import com.innovattic.medicinfo.logic.triage.model.ActionType.FINISH
import com.innovattic.medicinfo.logic.triage.model.ActionType.GO_TO_QUESTION
import com.innovattic.medicinfo.logic.triage.model.ActionType.GO_TO_QUESTIONNAIRE
import com.innovattic.medicinfo.logic.triage.model.ActionType.UNAUTHORIZED
import com.innovattic.medicinfo.logic.triage.model.Answer
import com.innovattic.medicinfo.logic.triage.model.BSN_QUESTION_UNIQUE_ID
import com.innovattic.medicinfo.logic.triage.model.Date
import com.innovattic.medicinfo.logic.triage.model.Descriptive
import com.innovattic.medicinfo.logic.triage.model.DescriptiveWithImages
import com.innovattic.medicinfo.logic.triage.model.MultipleChoice
import com.innovattic.medicinfo.logic.triage.model.OTHER_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.QuestionDefinition
import com.innovattic.medicinfo.logic.triage.model.QuestionType
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireModel
import com.innovattic.medicinfo.logic.triage.model.ResolvedAnswer
import com.innovattic.medicinfo.logic.triage.model.SingleChoice
import com.innovattic.medicinfo.logic.triage.model.Skip
import com.innovattic.medicinfo.logic.triage.model.StoredAnswer
import com.innovattic.medicinfo.logic.triage.model.Uncertain
import com.innovattic.medicinfo.logic.triage.model.UserProfile
import com.innovattic.medicinfo.logic.triage.tree.QuestionSchemaService
import org.apache.commons.lang3.LocaleUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.Clock
import java.util.*

val DUTCH_LOCALE = LocaleUtils.toLocale("nl")!!
val GERMAN_LOCALE = LocaleUtils.toLocale("de")!!
val ENGLISH_LOCALE = LocaleUtils.toLocale("en")!!
val VALID_LOCALES = listOf(DUTCH_LOCALE, GERMAN_LOCALE, ENGLISH_LOCALE)
const val SERVICE_UNAVAILABLE_VALUE = "Service unavailable"

/**
 * Holds a triage status, with answers and other associated data
 */
data class TriageCombinedData(
    val triageStatus: TriageStatus,
    val conversation: Conversation,
    val user: User,
    val label: Label,
    val questionnaireModel: QuestionnaireModel,
    val userProfile: UserProfile,
    val answers: List<TriageAnswer>,
) {

    fun getResolvedAnswers(answer: TriageAnswer): List<ResolvedAnswer> {
        val questionDefinition =
            questionnaireModel.getQuestionByUniqueId(userProfile.currentQuestionnaireName, answer.questionId)
        val storedAnswer: StoredAnswer = StoredAnswer.ofJson(answer.answer.data())
        return storedAnswer.resolveAnswers(questionDefinition)
    }

    /**
     * Find the highest urgency for a set of triage answers.
     * NOTE: urgencies rate from highest to lowest, ie. 'U1' is highest, 'U2' comes next and so forth
     *
     * @return highest urgency ResolvedAnswer, or null if no urgency in any of the answers
     */
    fun determineUrgencyFromAnswersString(): ResolvedAnswer? = answers.flatMap { getResolvedAnswers(it) }
        .filter { it.urgency != -1 }
        // min, not max! lowest number is the highest urgency
        .minByOrNull { it.urgency }
}

@Service
class TriageService(
    private val triageUserProfileService: TriageUserProfileService,
    private val triageStatusDao: TriageStatusDao,
    private val triageAnswerDao: TriageAnswerDao,
    private val triageImageService: TriageImageService,
    private val answerValidator: AnswerValidatorService,
    private val questionSchemaService: QuestionSchemaService,
    private val conversationDao: ConversationDao,
    private val userDao: UserDao,
    private val userService: UserService,
    private val labelDao: LabelDao,
    private val triageSalesforceService: TriageSalesforceService,
    private val triageReportingDao: TriageReportingDao,
    private val medicinfoServiceHoursProperties: MedicinfoServiceHoursProperties,
    private val clock: Clock,
    private val messageDao: MessageDao,
    private val triageRequireAgeProperties: TriagePreferBirthdateFeatureProperties,
    private val localazyService: LocalazyService,
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val RETURN = "RETURN"
    }

    /**
     * After updating the app triage will NOT be started if user has OPEN conversation with messages.
     * FINISHED status lets the app know that it should render chat.
     *
     * @param user user pojo
     * @param requestedMedicalArea nullable - medical area code, ex. 'KEELK'
     * @param lang nullable - language of the user that is a subject to triage.
     * NOTE: This method DOES NOT check if [lang] is a valid [Locale]
     */
    fun startTriage(user: User, requestedMedicalArea: String?, lang: Locale): TriageStateQuestionResponse {
        val conversation = conversationDao.getLatest(user.id)

        // business requirement is that if there is no conversation, or we have an archived one
        // we have to start a new conversation along with the triage.
        // Triage cannot exist without conversation
        if (conversation == null || conversation.status == ConversationStatus.ARCHIVED) {
            return startNewTriageIfNoActiveConversation(user, lang)
        }

        val triageStatus = triageStatusDao.getByConversationId(conversation.id)
        return if (triageStatus != null) {
            if (triageStatus.status == TriageProgress.EHIC_REQUIRED) {
                TriageStateQuestionResponse.ehicRequired(conversation)
            } else if (!isTriageAvailableForLocale(lang)) {
                stopTriageWithReason(triageStatus, TriageProgress.NOT_APPLICABLE, "lang=${lang.language}")
                TriageStateQuestionResponse.finished(conversation)
            } else if (triageStatus.status == TriageProgress.NOT_APPLICABLE && triageStatus.stopReason == SERVICE_UNAVAILABLE_VALUE) {
                TriageStateQuestionResponse.permanentlyClosed(conversation)
            } else if (!isBetweenServiceHours(user.labelId)) {
                // if started after service hours:
                // user can continue triage, upon completion will get automatic message that there is no one there
                // if user finished we want to get FINISHED from backend so the app can send user to the chat to check messages
                // user stopped the triage on medical area and now can continue it using the link from the nurse
                if (triageStatus.isStarted() || !triageStatus.active) {
                    startTriage(triageStatus, requestedMedicalArea, conversation)
                } else {
                    TriageStateQuestionResponse.notStarted()
                }
            } else {
                startTriage(triageStatus, requestedMedicalArea, conversation)
            }
        } else {
            if (hasMessages(conversation)) {
                TriageStateQuestionResponse.finished(conversation)
            } else {
                if (!isBetweenServiceHours(user.labelId)) {
                    TriageStateQuestionResponse.notStarted()
                } else {
                    val newTriage = saveNewTriage(conversation)
                    startTriage(newTriage, requestedMedicalArea, conversation)
                }
            }
        }
    }

    private fun startNewTriageIfNoActiveConversation(
        user: User,
        lang: Locale
    ): TriageStateQuestionResponse {
        if (!isBetweenServiceHours(user.labelId)) {
            return TriageStateQuestionResponse.notStarted()
        }

        val newConversation = conversationDao.create(user.id, user.labelId, ConversationStatus.OPEN, lang)
        val newTriage = saveNewTriage(newConversation)
        if (!userService.canUserStartNewTriageInSalesforce(user)) {
            stopTriageWithReason(newTriage, TriageProgress.NOT_APPLICABLE, reason = SERVICE_UNAVAILABLE_VALUE)
            return TriageStateQuestionResponse.permanentlyClosed(newConversation)
        }

        if (userService.userNeedsToPassEHICCheck(user)) {
            triageStatusDao.setEhicRequiredStatus(newTriage.id)
            return TriageStateQuestionResponse.ehicRequired(newConversation)
        }

        if (!isTriageAvailableForLocale(lang)) {
            stopTriageWithReason(newTriage, TriageProgress.NOT_APPLICABLE, "lang=${lang.language}")
            return TriageStateQuestionResponse.finished(newConversation)
        }

        val userProfile = triageUserProfileService.getCurrentUserProfile(newTriage)
        return TriageStateQuestionResponse.new(
            newConversation,
            localazyService.translateQuestionDefinition(
                newConversation.language, questionSchemaService.getLatestSchema().findFirstQuestionToAsk(userProfile)
            ),
            userProfile
        )
    }

    fun isTriageAvailableForLocale(lang: Locale) = VALID_LOCALES.contains(lang)

    private fun hasMessages(conversation: Conversation) = messageDao.count(conversation.id, null) > 0

    private fun startTriage(
        triageStatus: TriageStatus,
        medicalArea: String?,
        conversation: Conversation
    ): TriageStateQuestionResponse {
        // business requirement is that if triage was stopped by user(abandoned) on medical area question
        // and user wants to chat, then nurse can send user back to triage by providing a special message, that will
        // contain preselected medical area and user will continue from first question from that medical area questionnaire
        var updatedTriageStatus = triageStatus
        if ((
                triageStatus.status == TriageProgress.FINISHED_BY_USER_WITH_CHAT ||
                    triageStatus.status == TriageProgress.FINISHED_BY_USER_NO_MEDAREA
                ) && medicalArea != null
        ) {
            updatedTriageStatus =
                saveMedicalAreaAnswerIfNonExistent(updatedTriageStatus, medicalArea) ?: updatedTriageStatus
        }

        // User finished the OVERIGE questionnaire after the chat with a nurse. The nurse sends him back to fill in a
        // questionnaire on a specific medical area. This will update the medical area answer and sets it as its last
        // created answer so the normal flow can continue from the newly selected medical area.
        if (triageStatus.status == TriageProgress.FINISHED_BY_USER_OTHER && medicalArea != null) {
            updatedTriageStatus =
                updateMedicalAreaAnswer(updatedTriageStatus, medicalArea)
        }

        if (!updatedTriageStatus.active) {
            return TriageStateQuestionResponse.finished(conversation)
        }

        // If triageStatus.active == true, we're sure that the triage model is loaded by QuestionSchemaService
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")

        val userProfile = triageUserProfileService.getCurrentUserProfile(updatedTriageStatus)
        val nextQuestion = getNextUnansweredQuestion(userProfile, updatedTriageStatus)
            ?: error("Next question cannot be null when starting triage")

        return TriageStateQuestionResponse.inProgress(
            conversation,
            localazyService.translateQuestionDefinition(conversation.language, nextQuestion),
            isGoBackAllowed(updatedTriageStatus, nextQuestion, questionSchema, userProfile),
            questionSchema.calculateProgress(nextQuestion),
            userProfile
        )
    }

    /**
     *  Updates the answer to the question on which medical area the user needs help.
     *  By updating the answer we can continue with the normal flow when getting the next question.
     *  I.E. The next question will be the first question of the selected medical area questionnaire.
     */
    private fun updateMedicalAreaAnswer(triageStatus: TriageStatus, medicalArea: String): TriageStatus {
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")
        val question = questionSchema.medicalAreaQuestion
        val answer = question.answer.find { it.action.actionText == medicalArea }
                ?: throw IllegalArgumentException("Medical area $medicalArea passed as parameter is not present in answers")
        val triageAnswer = triageAnswerDao.findByTriageStatusIdAndQuestionId(triageStatus.id, question.uniqueQuestionId)
            ?: error(
                "No triage answer found with id: ${triageStatus.id} and question id: ${question.uniqueQuestionId}"
            )

        triageAnswerDao.updateTriageAnswer(
            id = triageAnswer.id,
            answerJson = SingleChoice(question.uniqueQuestionId, answer.id).toJson(),
            rawAnswerJson = SingleChoiceAnswer(question.uniqueQuestionId, answer.id).toJson()
        )
        return triageStatusDao.continueTriage(triageStatus.id)
    }

    private fun isBetweenServiceHours(labelId: Int): Boolean {
        val label = labelDao.getById(labelId) ?: error("No label with id $labelId")

        return medicinfoServiceHoursProperties.getServiceAvailability(clock, label.code).serviceAvailable
    }

    /**
     * DEPRECATED, is replaced with updateMedicalAreaAnswer,
     * since it's the new way of handling a no medical area selected.
     */
    private fun saveMedicalAreaAnswerIfNonExistent(triageStatus: TriageStatus, medicalArea: String): TriageStatus? {
        log.info("Deprecated flow for no medical area selected is used, function: saveMedicalAreaAnswerIfNonExistent")
        val userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)
        val currentUnansweredQuestion = getNextUnansweredQuestion(userProfile, triageStatus)
            ?: throw createResponseStatusException { "No more questions to be answered" }
        if (currentUnansweredQuestion.isMedicalAreaQuestion()) {
            val answer = currentUnansweredQuestion.answer.find { it.action.actionText == medicalArea }
                ?: throw IllegalArgumentException("Medical area $medicalArea passed as parameter is not present in answers")
            triageAnswerDao.saveNew(
                triageStatusId = triageStatus.id,
                questionId = currentUnansweredQuestion.uniqueQuestionId,
                answerJson = SingleChoice(currentUnansweredQuestion.uniqueQuestionId, answer.id).toJson(),
                rawAnswerJson = SingleChoiceAnswer(currentUnansweredQuestion.uniqueQuestionId, answer.id).toJson()
            )
            return triageStatusDao.continueTriage(triageStatus.id)
        }
        return null
    }

    private fun saveNewTriage(conversation: Conversation): TriageStatus = triageStatusDao.createTriageStatus(
        conversation.customerId,
        questionSchemaService.getLatestSchema().version,
        conversation.id
    )

    fun saveAnswer(user: User, answerRequest: AnswerRequest, supportsContinuation: Boolean): AnswerResponse {
        if (!supportsContinuation) {
            log.info("Old flow of the triage is used for userId=${user.publicId}")
        }
        val triageStatus = getActiveTriageForUser(user)
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")
        var userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)

        val existingAnswer =
            triageAnswerDao.findByTriageStatusIdAndQuestionId(triageStatus.id, answerRequest.questionId)
                ?.let { StoredAnswer.ofJson(it.answer.data()) }
        val currentQuestion = existingAnswer?.let {
            questionSchema.getQuestionByUniqueId(userProfile.currentQuestionnaireName, it.questionId)
        } ?: getNextUnansweredQuestion(userProfile, triageStatus)
        ?: throw createResponseStatusException { "No more questions to answer" }

        if (existingAnswer != null && !isReansweringAllowed(currentQuestion, triageStatus)) {
            throw createResponseStatusException { "Correction of profile question answer is not allowed" }
        }
        answerValidator.validate(triageStatus, currentQuestion, answerRequest)
        val newAnswer = answerRequest.toDomain()
        if (existingAnswer != newAnswer) {
            // when user changes the answer to some already answered question it can cause questionnaire to follow
            // different path that is why we need to remove current and all subsequent answers from the database
            triageAnswerDao.deleteAllWithIdGreaterOrEqualThan(triageStatus.id, newAnswer.questionId)
            triageAnswerDao.saveNew(
                triageStatusId = triageStatus.id,
                questionId = newAnswer.questionId,
                answerJson = newAnswer.toJson(),
                rawAnswerJson = answerRequest.toJson()
            )
        }

        val nextAction = determineNextAction(newAnswer, currentQuestion, triageStatus)
        if (
            finishTriageIfApplicable(
                triageStatus, nextAction, supportsContinuation,
                currentQuestion.questionnaireId == OTHER_QUESTIONNAIRE_ID
            )
        ) {
            return AnswerResponse.finished(nextAction)
        }

        // answering question can change user current questionnaire as well
        // as the person which is the target of a question
        userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)

        val nextAnswer = triageAnswerDao.getNextByUniqueId(triageStatus.id, newAnswer.questionId)
        val nextQuestion = if (nextAnswer == null) {
            // if user did not answer any questions beyond this point, use the latest-given answer to calculate
            // the next question in the questionnaire
            getNextUnansweredQuestion(userProfile, triageStatus)
        } else {
            // if the user did already answer the next question before, than we're re-answering questions.
            // getNextUnansweredQuestion would end up something further down in the questionnaire
            questionSchema.getQuestionByUniqueId(userProfile.currentQuestionnaireName, nextAnswer.questionId)
        }
        val isPreviousQuestionAllowed =
            nextQuestion?.let { isGoBackAllowed(triageStatus, it, questionSchema, userProfile) } ?: false

        val conversation = conversationDao.getLatestOpen(user.id)
            ?: throw createResponseStatusException(code = ErrorCodes.CONVERSATION_MISSING) { "User has no open conversation" }

        return AnswerResponse.of(
            nextQuestion?.let { QuestionResponse.of(localazyService.translateQuestionDefinition(conversation.language, it), userProfile) },
            nextQuestion?.let { questionSchema.calculateProgress(it) } ?: 1.0f,
            nextAction,
            isPreviousQuestionAllowed,
            nextAnswer?.let { AnswerRequest.ofJson(it.rawAnswer) },
            supportsContinuation
        )
    }

    private fun getActiveTriageForUser(user: User): TriageStatus {
        val conversation = conversationDao.getLatestOpen(user.id)
            ?: throw createResponseStatusException(code = ErrorCodes.CONVERSATION_MISSING) { "User has no open conversation" }

        return triageStatusDao.getActiveByConversationId(conversation.id)
            ?: throw createResponseStatusException { "User has no active triage" }
    }

    private fun determineNextAction(
        storedAnswer: StoredAnswer,
        currentQuestion: QuestionDefinition,
        triageStatus: TriageStatus,
    ): Action {
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")
        val userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)

        return when (storedAnswer) {
            is SingleChoice -> {
                val action = currentQuestion.getAnswerById(storedAnswer.answer).action
                if (action.actionText == RETURN) {
                    handleReturnAction(currentQuestion, triageStatus, action, questionSchema, userProfile)
                } else {
                    action
                }
            }
            is MultipleChoice -> {
                getActionFromMultiSelectQuestion(currentQuestion, storedAnswer, triageStatus)
            }
            is Descriptive, is DescriptiveWithImages, is Date -> {
                // those questions only have one answer
                val action = currentQuestion.answer[0].action
                if (action.actionText == RETURN) {
                    handleReturnAction(currentQuestion, triageStatus, action, questionSchema, userProfile)
                } else {
                    action
                }
            }
            is Skip, is Uncertain -> {
                // GO_TO_QUESTION and ASK_FOR_CHAT will always have nextQuestion
                // Retrieving the question and not the action of the question. On some questions the action is also
                // a high urgency popup. We only want to go to the question itself.
                val nextAction = currentQuestion.findFurthermostNextAction()
                if (nextAction?.actionText == RETURN) {
                    handleReturnAction(currentQuestion, triageStatus, nextAction, questionSchema, userProfile)
                } else if (nextAction?.nextQuestion == null) {
                    Action(FINISH, -1, null, "", null)
                } else {
                    Action(GO_TO_QUESTION, -1, null, "", nextAction.nextQuestion)
                }
            }
        }
    }

    private fun handleReturnAction(
        currentQuestion: QuestionDefinition,
        triageStatus: TriageStatus,
        action: Action,
        questionSchema: QuestionnaireModel,
        userProfile: UserProfile
    ): Action {
        val (previousQuestion, previousAnswer) =
            getCurrentMultiSelectQuestionAndAnswer(currentQuestion.uniqueQuestionId, triageStatus)
        val actionText = determineNextAction(previousAnswer, previousQuestion, triageStatus).actionText
        return action.copy(
            actionText = actionText,
            nextQuestion =
            questionSchema.getQuestionByUniqueId(userProfile.currentQuestionnaireName, actionText)
        )
    }

    private fun isQuestionAnswered(questionId: String, triageStatus: TriageStatus): Boolean {
        val allAnswers = triageAnswerDao.getAllByTriageStatusId(triageStatus.id)
        return allAnswers.any { it.questionId == questionId }
    }

    private fun getCurrentMultiSelectQuestionAndAnswer(
        questionId: String,
        triageStatus: TriageStatus
    ): Pair<QuestionDefinition, MultipleChoice> {
        val previousAnswer = triageAnswerDao.getPreviousByUniqueId(
            triageStatus.id, questionId
        ) ?: throw createResponseStatusException { "Triage with id=${triageStatus.id} has no previous answers" }
        val previousQuestionId = previousAnswer.questionId
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")
        val userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)
        val previousQuestion = questionSchema.getQuestionByUniqueId(
            userProfile.currentQuestionnaireName,
            previousQuestionId
        )

        (StoredAnswer.ofJson(previousAnswer.answer.data()) as? MultipleChoice)?.let {
            return Pair(previousQuestion, it)
        } ?: return getCurrentMultiSelectQuestionAndAnswer(previousQuestionId, triageStatus)
    }

    /**
     * Given a multi-select question, the stored answer to that question, and the current triage status,
     * determine the next action to take based on the stored answer and the current question's answers and
     * actions. This function handles both the main question and any sub-questions that may be required
     * based on the stored answer.
     *
     * @param currentQuestion The current multi-select question being asked
     * @param storedAnswer The previously stored answer to the current multi-select question
     * @param triageStatus The current triage status, which includes the previously answered questions and their corresponding actions
     * @param highestUrgentActionFromSubQuestion The highest urgent action from any sub-questions that have already been answered,
     *                                          or null if there are no sub-questions or they have not yet been answered
     * @return The next action to take based on the stored answer and the current question's answers and actions, with the highest
     *         urgent action type included
     * @throws ResponseStatusException if there are no answers left to be handled and the current question is the main question
     *                                 but there is no next main action defined
     */
    private fun getActionFromMultiSelectQuestion(
        currentQuestion: QuestionDefinition,
        storedAnswer: MultipleChoice,
        triageStatus: TriageStatus,
        highestUrgentActionFromSubQuestion: Action? = null
    ): Action {
        // Determine the highest urgent action, either from the sub-question or from the current question and answer
        val highestUrgentAction = highestUrgentActionFromSubQuestion
            ?.let { getActionMostUrgent(it, getHighestUrgentAction(currentQuestion, storedAnswer)) }
            ?: getHighestUrgentAction(currentQuestion, storedAnswer)

        // Determine the list of answers that need to be handled based on the stored answer and the triage status
        val answersToBeHandled = currentQuestion.getAnswersByIdIn(storedAnswer.answer)
            .filterNot { it.action.actionText == RETURN || isQuestionAnswered(it.action.actionText, triageStatus) }

        // Determine the most urgent action among the remaining answers, or
        // the first non-urgent answer if there are no urgent answers left
        val action = answersToBeHandled.minByOrNull { it.action.urgency }?.action
            ?: answersToBeHandled.firstOrNull { it.action.urgency == -1 }?.action

        // If there is an action to be taken, return it with the highest urgent action type
        if (action != null) {
            return action.copy(actionType = highestUrgentAction.actionType)
        }

        // If there are no answers left to be handled and the current question is a sub-question with a RETURN action,
        // recurse on the parent question
        if (currentQuestion.nextMainAction?.actionText == RETURN) {
            val (previousMultipleChoiceQuestion, previousMultipleChoiceAnswer) =
                getCurrentMultiSelectQuestionAndAnswer(currentQuestion.uniqueQuestionId, triageStatus)
            return getActionFromMultiSelectQuestion(
                previousMultipleChoiceQuestion,
                previousMultipleChoiceAnswer,
                triageStatus,
                highestUrgentAction
            )
        }

        // If there are no answers left to be handled and the current question is the main question,
        // return the next main action with the highest urgent action type
        return currentQuestion.nextMainAction?.copy(actionType = highestUrgentAction.actionType)
            ?: throw createResponseStatusException { "Answers matched with request answer ids has 0 size" }
    }

    private fun getHighestUrgentAction(
        currentQuestion: QuestionDefinition,
        storedAnswer: MultipleChoice
    ): Action {
        val allAnswers: List<Answer> = currentQuestion.getAnswersByIdIn(storedAnswer.answer)

        return allAnswers
            .filter { it.action.urgency != -1 }
            .minByOrNull { it.action.urgency }?.action

            ?: allAnswers
                .filter { it.action.urgency != -1 }
                .minByOrNull { it.action.urgency }?.action

            ?: currentQuestion.nextMainAction

            ?: throw createResponseStatusException { "Answers matched with request answer ids has 0 size" }
    }

    private fun finishTriageIfApplicable(
        triageStatus: TriageStatus,
        action: Action,
        supportsContinuation: Boolean,
        questionnaireIsOther: Boolean = false,
    ): Boolean {
        val triageProgress = when (action.actionType) {
            ASK_FOR_CHAT -> {
                if (supportsContinuation) return false else TriageProgress.FINISHED_BY_CHAT
            }
            FINISH -> {
                if (questionnaireIsOther) TriageProgress.FINISHED_BY_USER_OTHER else TriageProgress.FINISHED
            }
            UNAUTHORIZED -> TriageProgress.FINISHED_UNAUTHORIZED
            else -> return false
        }

        stopTriageWithReason(triageStatus, triageProgress, questionnaireIsOther = questionnaireIsOther)
        return true
    }

    fun stopTriageWithReason(
        triageStatus: TriageStatus,
        triageProgress: TriageProgress,
        reason: String? = null,
        questionnaireIsOther: Boolean = false,
        ) {
        val updatedTriageStatus = triageStatusDao.endTriageStatus(triageStatus.id, triageProgress, reason)
        if (updatedTriageStatus.status.shouldBeSentToSalesforce == null) {
            log.error("Something went wrong, you cannot have ${updatedTriageStatus.status} as one of the end statuses")
        }

        val triageData = fetchTriageData(updatedTriageStatus)
        if (updatedTriageStatus.status.shouldBeSentToSalesforce == true) {
            triageSalesforceService.sendTriageAnswers(triageData, questionnaireIsOther)
        } else {
            if (updatedTriageStatus.isRestartStatus()) {
                // we should archive the conversation as we want user to go to the homescreen and start triage over
                conversationDao.archive(triageData.conversation.publicId, triageData.user.id, triageData.label.id)
            }
        }
        writeTriageReportingData(triageData, reason)
        cleanupImages(updatedTriageStatus)
    }

    private fun getNextUnansweredQuestion(userProfile: UserProfile, triageStatus: TriageStatus): QuestionDefinition? {
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")
        // return first question if triage was started but no questions were asked
        val latestStoredAnswer = triageAnswerDao.getLatestBy(triageStatus.id)
            ?: return questionSchema.findFirstQuestionToAsk(userProfile)

        val latestAnswer = StoredAnswer.ofJson(latestStoredAnswer.answer.data())
        val answeredQuestion =
            questionSchema.getQuestionByUniqueId(userProfile.currentQuestionnaireName, latestAnswer.questionId)

        val action = determineNextAction(latestAnswer, answeredQuestion, triageStatus)
        val nextQuestion = when {
            action.actionType == GO_TO_QUESTIONNAIRE -> questionSchema.findFirstQuestionToAsk(userProfile)
            action.nextQuestion == null -> null
            else -> {
                if (action.nextQuestion.canAsk(userProfile)) {
                    action.nextQuestion
                } else {
                    questionSchema.getNextAskableQuestion(action.nextQuestion, userProfile)
                }
            }
        }

        nextQuestion?.let {
            return customizeQuestion(nextQuestion, userProfile)
        }

        return nextQuestion
    }

    fun stopTriage(user: User, stopTriageRequest: StopTriageRequest) {
        val triageStatus = getActiveTriageForUser(user)

        val triageProgress = when {
            stopTriageRequest.stopReason == StopReason.HIGH_URGENCY_CHAT -> TriageProgress.FINISHED_BY_CHAT
            stopTriageRequest.stopReason == StopReason.NO_MEDICAL_AREA -> TriageProgress.FINISHED_BY_USER_NO_MEDAREA
            stopTriageRequest.stopReason == StopReason.WANTS_CHAT ||
                stopTriageRequest.wantsChat -> TriageProgress.FINISHED_BY_USER_WITH_CHAT
            else -> TriageProgress.FINISHED_BY_USER_WITHOUT_CHAT
        }

        stopTriageWithReason(triageStatus, triageProgress, stopTriageRequest.reason)
    }

    fun continueTriage(user: User): TriageStateQuestionResponse {
        val conversation = conversationDao.getLatest(user.id)

        failResponseIf(conversation == null, HttpStatus.NOT_FOUND, ErrorCodes.CONVERSATION_MISSING) {
            "No conversation for user with id: ${user.id} can be found."
        }

        val triageStatus = triageStatusDao.getByConversationId(conversation.id)
            ?: error("No triage found for conversation with id: ${conversation.id}")

        val continuedTriageStatus = triageStatusDao.continueTriage(triageStatus.id, TriageProgress.STARTED)
        return startTriage(continuedTriageStatus, null, conversation)
    }

    private fun cleanupImages(triageStatus: TriageStatus) {
        val schema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)!!

        // cleanup any images that were uploaded for this triage, but ended up
        // being not used
        val imageIds = mutableSetOf<String>()
        val answers = triageAnswerDao.getAllByTriageStatusId(triageStatus.id)
        val medicalArea = triageUserProfileService.getMedicalAreaFromAnswerList(schema, answers)
        if (medicalArea != null) {
            answers.forEach { answer ->
                val question = schema.getQuestionByUniqueId(medicalArea, answer.questionId)
                when (question.questionType) {
                    QuestionType.DESCRIPTIVE_WITH_PHOTO -> {
                        val parsedAnswer = StoredAnswer.ofJson(answer.answer.data())
                        // NOTE: parsedAnswer can also be of type 'Skip' if the user skipped the question
                        if (parsedAnswer is DescriptiveWithImages) {
                            imageIds.addAll(parsedAnswer.imageIds)
                        }
                    }
                    else -> {
                        // no images
                    }
                }
            }
        }
        val labelCode = triageStatusDao.getLabelCode(triageStatus)
        triageImageService.cleanup(labelCode, triageStatus.id, imageIds)
    }

    fun uploadImage(user: User, contentType: String, input: InputStream): ImageUploadResponse {
        val triageStatus = getActiveTriageForUser(user)
        val labelCode = triageStatusDao.getLabelCode(triageStatus)
        val uploadedImageId = triageImageService.upload(labelCode, triageStatus.id, contentType, input)
        return ImageUploadResponse(uploadedImageId)
    }

    fun downloadImage(user: User, imageId: String): Download {
        val triageStatus = getActiveTriageForUser(user)
        val labelCode = triageStatusDao.getLabelCode(triageStatus)
        return triageImageService.download(labelCode, triageStatus.id, imageId)
    }

    fun getQuestionBefore(user: User, questionId: String, supportsContinuation: Boolean): AnswerResponse {
        val triageStatus = getActiveTriageForUser(user)
        val userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")

        val currentQuestion =
            questionSchema.getQuestionByUniqueId(userProfile.currentQuestionnaireName, questionId)
        if (!isGoBackAllowed(triageStatus, currentQuestion, questionSchema, userProfile)) {
            throw createResponseStatusException { "Going back to previous question is not allowed at this point" }
        }

        // get previous or latest:
        // if user has already answered the targetQuestion, just pick the answer before that
        // if user has not given an answer yet for targetQuestion, go back to the lastest-answered question
        val previousAnswer = getPreviousOrLatestAnswer(triageStatus, currentQuestion.uniqueQuestionId)
        val storedAnswer = StoredAnswer.ofJson(previousAnswer.answer.data())
        val prevQuestionDefinition =
            questionSchema.getQuestionByUniqueId(userProfile.currentQuestionnaireName, storedAnswer.questionId)

        val conversation = conversationDao.getLatestOpen(user.id)
            ?: throw createResponseStatusException(code = ErrorCodes.CONVERSATION_MISSING) { "User has no open conversation" }

        return AnswerResponse.of(
            nextQuestion = QuestionResponse.of(
                localazyService.translateQuestionDefinition(conversation.language, prevQuestionDefinition),
                userProfile
            ),
            progress = questionSchema.calculateProgress(prevQuestionDefinition),
            action = determineNextAction(storedAnswer, prevQuestionDefinition, triageStatus),
            nextAnswer = AnswerRequest.ofJson(previousAnswer.rawAnswer),
            isGoBackAllowed = isGoBackAllowed(triageStatus, prevQuestionDefinition, questionSchema, userProfile),
            supportsContinuation = supportsContinuation
        )
    }

    private fun getPreviousOrLatestAnswer(
        triageStatus: TriageStatus,
        questionId: String
    ): TriageAnswer = triageAnswerDao.findByTriageStatusIdAndQuestionId(triageStatus.id, questionId)
        ?.let { triageAnswerDao.getPreviousByUniqueId(triageStatus.id, it.questionId) }
        ?: triageAnswerDao.getLatestBy(triageStatus.id)
        ?: throw createResponseStatusException { "Triage with id=${triageStatus.id} has no answers" }

    private fun isGoBackAllowed(
        triageStatus: TriageStatus,
        currentQuestion: QuestionDefinition,
        questionnaireModel: QuestionnaireModel,
        userProfile: UserProfile
    ): Boolean {
        if (currentQuestion.isProfileQuestion()) return false
        // check if medical area was prefilled by a nurse, if yes then going back to that question to change it is not allowed
        if (questionnaireModel.findFirstQuestionToAsk(userProfile).uniqueQuestionId == currentQuestion.uniqueQuestionId &&
            triageStatus.isContinued()
        ) {
            return false
        }
        return true
    }

    /**
     * This method checks if question can be re-answered. Business rules disallow re-answering of
     * profile questions excluding medical area question when it was NOT prefilled by nurse
     */
    private fun isReansweringAllowed(currentQuestion: QuestionDefinition, triageStatus: TriageStatus) =
        (currentQuestion.isMedicalAreaQuestion() && !triageStatus.isContinued()) || !currentQuestion.isProfileQuestion()

    /**
     * Fetch triage with all related objects for easy processing.
     */
    fun fetchTriageData(triageStatus: TriageStatus): TriageCombinedData {
        val user = userDao.getById(triageStatus.userId)
            ?: error("No user found for triageStatusId=${triageStatus.id}")
        val conversation = conversationDao.getLatest(user.id)
            ?: throw createResponseStatusException { "No conversation found for userId=${user.id}" }
        val label = labelDao.getById(user.labelId)
            ?: error("Label from user with userId=${user.id} not found for labelId=${user.labelId}")
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")
        val userProfile = triageUserProfileService.getCurrentUserProfile(triageStatus)
        val allAnswers = triageAnswerDao.getAllByTriageStatusId(triageStatus.id)

        return TriageCombinedData(triageStatus, conversation, user, label, questionSchema, userProfile, allAnswers)
    }

    private fun writeTriageReportingData(data: TriageCombinedData, endReason: String?) {

        val reportingAnswers = mutableListOf<ReportingTriageAnswer>()
        var filledQuestionnaireOther = false

        triageReportingDao.cleanExisting(data.triageStatus.id)

        data.answers.forEach { answer ->
            val questionDefinition =
                data.questionnaireModel.getQuestionByUniqueId(
                    data.userProfile.currentQuestionnaireName, answer.questionId
                )

            if (questionDefinition.questionnaireId == OTHER_QUESTIONNAIRE_ID) {
                filledQuestionnaireOther = true
            }

            val allPossibleAnswers = questionDefinition.answer
                .filter { it.answerText.isNotEmpty() }
                .takeUnless { it.isEmpty() }
                ?.joinToString(SALESFORCE_ANSWER_SEPARATOR) { it.answerText }
            val resolvedAnswers = data.getResolvedAnswers(answer)

            resolvedAnswers.forEach { singleAnswer ->
                reportingAnswers.add(
                    ReportingTriageAnswer(
                        null,
                        data.triageStatus.id,
                        answer.questionId,
                        questionDefinition.shortQuestionVpk,
                        singleAnswer.answerText,
                        singleAnswer.isDivergent,
                        allPossibleAnswers
                    )
                )
            }
        }

        val triageProgressPercentage = reportingAnswers.lastOrNull()
            ?.let {
                data.questionnaireModel.getQuestionByUniqueId(
                    data.userProfile.currentQuestionnaireName,
                    it.questionId
                )
            }
            ?.let { (data.questionnaireModel.calculateProgress(it) * 100).toInt() }
            ?: 0

        val highestUrgencyAnswer = data.determineUrgencyFromAnswersString()

        triageReportingDao.saveTriage(
            ReportingTriage(
                data.triageStatus.id,
                data.label.code,
                data.user.publicId,
                data.conversation.publicId,
                data.triageStatus.created,
                data.triageStatus.ended,
                data.userProfile.currentQuestionnaireName,
                data.userProfile.isAppUser,
                data.triageStatus.schemaVersion,
                data.triageStatus.status.isAbandoned,
                endReason,
                highestUrgencyAnswer?.urgencyName,
                data.triageStatus.status,
                triageProgressPercentage,
                filledQuestionnaireOther,
            )
        )

        triageReportingDao.saveAnswers(reportingAnswers)
    }

    /**
     * Check if birthdate is configured to be required
     *
     * @param user user object
     */
    fun preferBirthdateOverAge(user: User): Boolean {
        val userLabel = labelDao.getById(user.labelId)
            ?: error("User with uuid=${user.publicId} has non-existent label with id=${user.labelId}")

        return triageRequireAgeProperties.preferBirthdateOverAgeForLabel(userLabel.code)
    }

    /**
     * Function to specify custom modifications to questions.
     */
    private fun customizeQuestion(question: QuestionDefinition, userProfile: UserProfile): QuestionDefinition {
        /**
         * Requirement: if BSN question is asked in triage (for someone else), this question should be optional
         * if the main user's proposition is FOREIGN_TOURIST (foreigners don't have a BSN).
         */

        if (question.uniqueQuestionId == BSN_QUESTION_UNIQUE_ID) {
            if (userProfile.entryType == CustomerEntryType.HOLIDAY_FOREIGN_TOURIST.salesforceTranslation) {
                question.isQuestionRequired = false
            }
        }
        return question
    }

    /**
     * Returns the action which has the most urgency. If both are equal or
     * both are non-urgent then the first action is returned
     */
    private fun getActionMostUrgent(a: Action, b: Action): Action {
        if (b.urgency != -1 && b.urgency < a.urgency) return b
        return a
    }
}
