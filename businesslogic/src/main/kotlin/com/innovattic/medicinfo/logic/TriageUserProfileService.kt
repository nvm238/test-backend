package com.innovattic.medicinfo.logic

import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.TriageAnswerDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageAnswer
import com.innovattic.medicinfo.dbschema.tables.pojos.TriageStatus
import com.innovattic.medicinfo.logic.triage.model.Date
import com.innovattic.medicinfo.logic.triage.model.Descriptive
import com.innovattic.medicinfo.logic.triage.model.PROFILE_QUESTIONNAIRE_ID
import com.innovattic.medicinfo.logic.triage.model.QUESTION_WHO_ANSWER_ID_FOR_SELF
import com.innovattic.medicinfo.logic.triage.model.QuestionnaireModel
import com.innovattic.medicinfo.logic.triage.model.SingleChoice
import com.innovattic.medicinfo.logic.triage.model.StoredAnswer
import com.innovattic.medicinfo.logic.triage.model.UserProfile
import com.innovattic.medicinfo.logic.triage.model.getGenderFromAnswer
import com.innovattic.medicinfo.logic.triage.tree.QuestionSchemaService
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class TriageUserProfileService(
    private val userDao: UserDao,
    private val labelDao: LabelDao,
    private val triageAnswerDao: TriageAnswerDao,
    private val questionSchemaService: QuestionSchemaService,
    private val clock: Clock
) {

    /**
     * This method returns user profile of the user that is filling the questionnaire.
     * Until the user fills the profile questionnaire it is always the system user, after
     * answering all profile questions it can be either, system user or a user assembled
     * from questionnaire questions(ghost user)
     */
    fun getCurrentUserProfile(triageStatus: TriageStatus): UserProfile {
        val questionSchema = questionSchemaService.getQuestionSchema(triageStatus.schemaVersion)
            ?: error("No question schema with version=${triageStatus.schemaVersion}")
        val systemUserProfile = assembleTriageUserFromSystemUser(triageStatus, questionSchema)
        val personTriageAnswer = triageAnswerDao.findByTriageStatusIdAndQuestionId(
            triageStatus.id,
            questionSchema.whoProfileQuestion.uniqueQuestionId
        )
            ?: return systemUserProfile
        val personAnswer = StoredAnswer.ofJson(personTriageAnswer.answer.data()) as SingleChoice

        return if (personAnswer.answer == QUESTION_WHO_ANSWER_ID_FOR_SELF) {
            systemUserProfile
        } else {
            assembleUserInfoFromProfileQuestions(triageStatus, questionSchema, systemUserProfile)
        }
    }

    /**
     * Given the answer to the medical area answer, return the questionnaire name for the medical area
     */
    private fun getMedicalAreaFromAnswer(questionnaireModel: QuestionnaireModel, answer: TriageAnswer): String {
        val storedAnswer = StoredAnswer.ofJson(answer.answer.data()) as SingleChoice
        val answerId = storedAnswer.answer
        return questionnaireModel.medicalAreaQuestion.getAnswerById(answerId).action.actionText
    }

    /**
     * Given the list of answers for a triage, find the selected medical area, if any.
     */
    fun getMedicalAreaFromAnswerList(questionnaireModel: QuestionnaireModel, answers: List<TriageAnswer>): String? {
        val answer = answers.find { it.questionId == questionnaireModel.medicalAreaQuestion.uniqueQuestionId }
        answer ?: return null
        return getMedicalAreaFromAnswer(questionnaireModel, answer)
    }

    private fun assembleTriageUserFromSystemUser(
        triageStatus: TriageStatus,
        questionnaireModel: QuestionnaireModel
    ): UserProfile {
        val user = userDao.getById(triageStatus.userId)
            ?: error("No user with id=${triageStatus.userId}")

        val label = labelDao.getById(user.labelId)
            ?: error("No label with id=${user.labelId} for userId=${triageStatus.userId}")

        val medicalAreaTriageAnswer = triageAnswerDao.findByTriageStatusIdAndQuestionId(
            triageStatus.id,
            questionnaireModel.medicalAreaQuestion.uniqueQuestionId
        )

        val medicalArea = medicalAreaTriageAnswer?.let { getMedicalAreaFromAnswer(questionnaireModel, it) }
            ?: PROFILE_QUESTIONNAIRE_ID

        val age = if (user.birthdate == null) {
            user.age
        } else {
            ChronoUnit.YEARS.between(user.birthdate, LocalDateTime.now(clock)).toInt()
        } ?: error("User birthdate or at least age is required to ask questions in triage")

        return UserProfile(
            age,
            user.gender,
            medicalArea,
            true,
            label.code,
            user.entryType
        )
    }

    private fun assembleUserInfoFromProfileQuestions(
        triageStatus: TriageStatus,
        questionnaireModel: QuestionnaireModel,
        systemUserProfile: UserProfile
    ): UserProfile {
        val genderTriageAnswer = triageAnswerDao.findByTriageStatusIdAndQuestionId(
            triageStatus.id,
            questionnaireModel.genderProfileQuestion.uniqueQuestionId
        )
            ?: return systemUserProfile
        val birthdayTriageAnswer = triageAnswerDao.findByTriageStatusIdAndQuestionId(
            triageStatus.id,
            questionnaireModel.birthdayProfileQuestion.uniqueQuestionId
        )
            ?: return systemUserProfile

        val lastAskableQuestion = questionnaireModel.getQuestionnaire(PROFILE_QUESTIONNAIRE_ID)
            ?.questions
            ?.filterNot { it.uniqueQuestionId == questionnaireModel.medicalAreaQuestion.uniqueQuestionId }
            ?.last { it.canAsk(systemUserProfile) }
            ?: error("Profile questionnaire does not exist!")
        triageAnswerDao.findByTriageStatusIdAndQuestionId(
            triageStatus.id,
            lastAskableQuestion.uniqueQuestionId
        ) ?: return systemUserProfile

        // temporary solution until iOS an Android implement date handling
        val birthdayOrAgeAnswer = StoredAnswer.ofJson(birthdayTriageAnswer.answer.data())
        val age = when (birthdayOrAgeAnswer) {
            is Date -> ChronoUnit.YEARS.between(birthdayOrAgeAnswer.answer, LocalDate.now(clock)).toInt()
            is Descriptive -> try {
                birthdayOrAgeAnswer.answer.toInt()
            } catch (e: NumberFormatException) {
                error("Age is not a number!")
            }
            else -> error("Age is not a parsable type!")
        }

        val genderAnswer = StoredAnswer.ofJson(genderTriageAnswer.answer.data())
        val gender = getGenderFromAnswer(genderAnswer)

        val currentQuestionnaireId = triageAnswerDao.findByTriageStatusIdAndQuestionId(
            triageStatus.id,
            questionnaireModel.medicalAreaQuestion.uniqueQuestionId
        )?.let { getMedicalAreaFromAnswer(questionnaireModel, it) }
            ?: PROFILE_QUESTIONNAIRE_ID

        return UserProfile(age, gender, currentQuestionnaireId, false, systemUserProfile.labelCode, systemUserProfile.entryType)
    }
}
