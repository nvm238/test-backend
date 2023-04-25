package com.innovattic.medicinfo.logic.triage

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.TriageStatusDao
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import com.innovattic.medicinfo.logic.TriageImageService
import com.innovattic.medicinfo.logic.dto.triage.AnswerRequest
import com.innovattic.medicinfo.logic.dto.triage.BooleanAnswer
import com.innovattic.medicinfo.logic.dto.triage.DateAnswer
import com.innovattic.medicinfo.logic.dto.triage.MultipleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.ImagesAnswer
import com.innovattic.medicinfo.logic.dto.triage.SingleChoiceAnswer
import com.innovattic.medicinfo.logic.dto.triage.SkipAnswer
import com.innovattic.medicinfo.logic.dto.triage.SliderAnswer
import com.innovattic.medicinfo.logic.dto.triage.StringAnswer
import com.innovattic.medicinfo.logic.dto.triage.UncertainAnswer
import com.innovattic.medicinfo.logic.triage.model.QuestionDefinition
import com.innovattic.medicinfo.logic.triage.model.QuestionType.BOOLEAN
import com.innovattic.medicinfo.logic.triage.model.QuestionType.DATE
import com.innovattic.medicinfo.logic.triage.model.QuestionType.DESCRIPTIVE
import com.innovattic.medicinfo.logic.triage.model.QuestionType.DESCRIPTIVE_WITH_PHOTO
import com.innovattic.medicinfo.logic.triage.model.QuestionType.MULTI_SELECTION
import com.innovattic.medicinfo.logic.triage.model.QuestionType.NUMBER
import com.innovattic.medicinfo.logic.triage.model.QuestionType.SINGLE_SELECTION
import com.innovattic.medicinfo.logic.triage.model.QuestionType.SINGLE_LINE_DESCRIPTIVE
import com.innovattic.medicinfo.logic.triage.model.QuestionType.SLIDER
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AnswerValidatorService(
    private val imageService: TriageImageService,
    private val triageStatusDao: TriageStatusDao,
    private val clock: Clock
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private fun validateDescription(desc: String) {
        if (desc.length > 2048) {
            throw createResponseStatusException { "Description of an answer is too long!" }
        }
    }

    fun validate(triageStatus: TriageStatus, currentQuestion: QuestionDefinition, answerRequest: AnswerRequest) {
        if (currentQuestion.uniqueQuestionId != answerRequest.questionId) {
            throw createResponseStatusException {
                """User should be answering questionId=${currentQuestion.uniqueQuestionId},
                    but answer contains questionId=${answerRequest.questionId}"""
            }
        }
        if (validateSkip(currentQuestion, answerRequest)) return
        if (validateUncertain(currentQuestion, answerRequest)) return
        validateQuestionType(triageStatus, currentQuestion, answerRequest)
    }

    private fun validateQuestionType(
        triageStatus: TriageStatus,
        currentQuestion: QuestionDefinition,
        answerRequest: AnswerRequest
    ): Boolean {
        when (currentQuestion.questionType) {
            SINGLE_SELECTION -> {
                if (answerRequest !is SingleChoiceAnswer) {
                    throw createResponseStatusException {
                        "Answer for questionId=${currentQuestion.uniqueQuestionId} should be a single ID and type single_selection"
                    }
                }

                currentQuestion.getAnswerById(answerRequest.answer)
            }
            BOOLEAN -> {
                if (answerRequest !is BooleanAnswer) {
                    throw createResponseStatusException {
                        "Answer for questionId=${currentQuestion.uniqueQuestionId} should be a single ID and type boolean"
                    }
                }

                currentQuestion.getAnswerById(answerRequest.answer)
            }
            SLIDER -> {
                if (answerRequest !is SliderAnswer) {
                    throw createResponseStatusException {
                        "Answer for questionId=${currentQuestion.uniqueQuestionId} should be a single ID and type slider"
                    }
                }

                currentQuestion.getAnswerById(answerRequest.answer)
            }
            MULTI_SELECTION -> {
                if (answerRequest !is MultipleChoiceAnswer) {
                    throw createResponseStatusException {
                        """Answer for questionId=${currentQuestion.uniqueQuestionId} 
                            should be an array of ids ID"""
                    }
                }

                answerRequest.answer.map { currentQuestion.getAnswerById(it) }
            }
            DESCRIPTIVE, SINGLE_LINE_DESCRIPTIVE -> {
                if (answerRequest !is StringAnswer) {
                    throw createResponseStatusException {
                        """Answer for questionId=${currentQuestion.uniqueQuestionId} 
                            should be a string"""
                    }
                }

                validateDescription(answerRequest.answer)
            }
            NUMBER -> {
                if (answerRequest !is StringAnswer) {
                    throw createResponseStatusException {
                        """Answer for questionId=${currentQuestion.uniqueQuestionId} 
                            should be a number inside quotes"""
                    }
                }
                try {
                    answerRequest.answer.toInt()
                } catch (ex: NumberFormatException) {
                    log.debug("Answer for questionId=${currentQuestion.uniqueQuestionId} should be a number", ex)
                    throw createResponseStatusException {
                        "Answer for questionId=${currentQuestion.uniqueQuestionId} should be a number"
                    }
                }
            }
            DATE -> {
                if (answerRequest !is DateAnswer) {
                    throw createResponseStatusException {
                        """Answer for questionId=${currentQuestion.uniqueQuestionId} 
                            should be a date inside quotes"""
                    }
                }
                val age = ChronoUnit.YEARS.between(answerRequest.answer, LocalDate.now(clock))

                if (age !in 0..125) {
                    throw createResponseStatusException { "Birthday has to be serious value | given date=${answerRequest.answer}" }
                }
            }
            DESCRIPTIVE_WITH_PHOTO -> {
                if (answerRequest !is ImagesAnswer) {
                    throw createResponseStatusException {
                        """Answer for questionId=${currentQuestion.uniqueQuestionId} 
                            should be of type images"""
                    }
                }

                validateDescription(answerRequest.description)

                val labelCode = triageStatusDao.getLabelCode(triageStatus)
                answerRequest.imageIds.forEach {
                    if (!imageService.exist(labelCode, triageStatus.id, it)) {
                        throw createResponseStatusException { "Image $it not found in triage" }
                    }
                }

                if (answerRequest.description.isEmpty() && answerRequest.imageIds.isEmpty()) {
                    throw createResponseStatusException { "Non-empty description or at least one image required" }
                }
            }
            else -> {
                throw UnsupportedOperationException("Unsupported questionType=${currentQuestion.questionType}")
            }
        }
        return true
    }

    private fun validateSkip(currentQuestion: QuestionDefinition, answerRequest: AnswerRequest): Boolean {
        if (answerRequest is SkipAnswer) {
            return if (currentQuestion.isQuestionRequired) {
                throw createResponseStatusException { "Question ${currentQuestion.uniqueQuestionId} is required and cannot be skipped" }
            } else {
                true
            }
        }
        return false
    }

    private fun validateUncertain(currentQuestion: QuestionDefinition, answerRequest: AnswerRequest): Boolean {
        if (answerRequest is UncertainAnswer) {
            return if (currentQuestion.isDontKnow) {
                true
            } else {
                throw createResponseStatusException { "Question ${currentQuestion.uniqueQuestionId} does not allow uncertain answer" }
            }
        }
        return false
    }
}
