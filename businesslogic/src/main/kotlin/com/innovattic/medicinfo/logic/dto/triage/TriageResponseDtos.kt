package com.innovattic.medicinfo.logic.dto.triage

import com.fasterxml.jackson.annotation.JsonInclude
import com.innovattic.common.database.databaseUtcToZoned
import com.innovattic.medicinfo.database.dto.ConversationStatus
import com.innovattic.medicinfo.dbschema.tables.pojos.Conversation
import com.innovattic.medicinfo.logic.triage.model.Action
import com.innovattic.medicinfo.logic.triage.model.ActionType
import com.innovattic.medicinfo.logic.triage.model.QuestionDefinition
import com.innovattic.medicinfo.logic.triage.model.UserProfile
import java.time.ZonedDateTime
import java.util.*

enum class TriageState {
    IN_PROGRESS,
    NEW,
    FINISHED,
    NOT_STARTED,
    PERMANENTLY_CLOSED,
    EHIC_REQUIRED,
}

data class TriageStateQuestionResponse private constructor(
    val triageStatus: TriageState,
    val conversation: ConversationResponse?,
    val progress: Float,
    val question: QuestionResponse? = null,
    val isGoBackAllowed: Boolean = false
) {
    companion object {
        fun notStarted(): TriageStateQuestionResponse = TriageStateQuestionResponse(TriageState.NOT_STARTED, null, 0.0f)

        fun permanentlyClosed(conversation: Conversation): TriageStateQuestionResponse =
            TriageStateQuestionResponse(TriageState.PERMANENTLY_CLOSED, ConversationResponse.of(conversation), 0.0f)

        fun ehicRequired(conversation: Conversation): TriageStateQuestionResponse =
            TriageStateQuestionResponse(TriageState.EHIC_REQUIRED, ConversationResponse.of(conversation), 0.0f)

        fun finished(conversation: Conversation): TriageStateQuestionResponse =
            TriageStateQuestionResponse(TriageState.FINISHED, ConversationResponse.of(conversation), 1.0f)

        fun new(
            conversation: Conversation,
            questionDefinition: QuestionDefinition,
            userProfile: UserProfile
        ): TriageStateQuestionResponse =
            TriageStateQuestionResponse(
                TriageState.NEW,
                ConversationResponse.of(conversation),
                0.0f,
                QuestionResponse.of(questionDefinition, userProfile)
            )

        fun inProgress(
            conversation: Conversation,
            questionDefinition: QuestionDefinition,
            isGoBackAllowed: Boolean,
            progress: Float,
            userProfile: UserProfile
        ): TriageStateQuestionResponse = TriageStateQuestionResponse(
            TriageState.IN_PROGRESS,
            ConversationResponse.of(conversation),
            progress,
            QuestionResponse.of(questionDefinition, userProfile),
            isGoBackAllowed
        )
    }
}

data class ConversationResponse(
    val id: UUID,
    val created: ZonedDateTime,
    val status: ConversationStatus,
) {
    companion object {
        fun of(conversation: Conversation): ConversationResponse =
            ConversationResponse(
                conversation.publicId,
                databaseUtcToZoned(conversation.created),
                conversation.status
            )
    }
}

data class AnswerResponse private constructor(
    val nextQuestion: QuestionResponse?,
    val progress: Float,
    val action: AnswerActionResponse,
    val urgency: Int,
    val nextAnswer: AnswerRequest?,
    val isGoBackAllowed: Boolean
) {
    companion object {
        fun finished(action: Action): AnswerResponse = of(null, 1.0f, action, false, null, false)

        fun of(
            nextQuestion: QuestionResponse?,
            progress: Float,
            action: Action,
            isGoBackAllowed: Boolean,
            nextAnswer: AnswerRequest? = null,
            supportsContinuation: Boolean
        ): AnswerResponse {
            val answerAction = when (action.actionType) {
                ActionType.SKIP_NEXT, ActionType.NEXT, ActionType.GO_TO_QUESTION, ActionType.GO_TO_QUESTIONNAIRE -> {
                    AnswerActionResponse.NEXT
                }
                ActionType.ASK_FOR_CHAT -> if (supportsContinuation) AnswerActionResponse.ASK_FOR_CHAT else AnswerActionResponse.GO_TO_CHAT
                ActionType.FINISH -> AnswerActionResponse.FINISH
                ActionType.UNAUTHORIZED -> AnswerActionResponse.UNAUTHORIZED
            }

            return AnswerResponse(
                nextQuestion = nextQuestion,
                progress = progress,
                action = answerAction,
                urgency = action.urgency,
                nextAnswer = nextAnswer,
                isGoBackAllowed = isGoBackAllowed
            )
        }
    }
}

enum class AnswerActionResponse {
    NEXT,
    GO_TO_CHAT,
    ASK_FOR_CHAT,
    FINISH,
    UNAUTHORIZED
}

data class QuestionResponse(
    val questionId: String,
    val title: String,
    val description: String,
    val image: String,
    val questionType: String,
    val isRequired: Boolean,
    val isUncertainAllowed: Boolean,
    val answers: List<QuestionDefinitionAnswerResponse>,
    val metadata: Any?
) {
    companion object {
        fun of(questionDefinition: QuestionDefinition, userProfile: UserProfile): QuestionResponse {
            return QuestionResponse(
                questionId = questionDefinition.uniqueQuestionId,
                title = questionDefinition.getQuestionTextFor(userProfile),
                description = questionDefinition.additionalInfo,
                image = questionDefinition.pictureWithQuestion,
                questionType = questionDefinition.questionType.toString(),
                isRequired = questionDefinition.isQuestionRequired,
                isUncertainAllowed = questionDefinition.isDontKnow,
                answers = questionDefinition.answer
                    .filter { it.isApplicable(userProfile) }
                    .map {
                        QuestionDefinitionAnswerResponse(
                            id = it.id,
                            answerText = it.answerText,
                            alternativeNames = it.alternativeNames,
                            action = it.action.actionType
                        )
                    },
                metadata = ""
            )
        }
    }
}

data class QuestionDefinitionAnswerResponse(
    val id: Int,
    val answerText: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val alternativeNames: List<String>,
    val action: ActionType
)
