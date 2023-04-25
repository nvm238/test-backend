package com.innovattic.medicinfo.logic

import com.innovattic.common.database.databaseUtcToZoned
import com.innovattic.common.error.createResponseStatusException
import com.innovattic.common.error.failResponseIf
import com.innovattic.common.notification.PushNotificationService
import com.innovattic.medicinfo.database.dao.ConfigurePushNotificationMessageDto
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dto.LabelDto
import com.innovattic.medicinfo.database.dto.UserRole
import com.innovattic.medicinfo.dbschema.tables.pojos.Label
import com.innovattic.medicinfo.dbschema.tables.pojos.User
import com.innovattic.medicinfo.logic.dto.UpdateLabelDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*

@Component
class LabelService(private val dao: LabelDao, private val notificationService: PushNotificationService) {
    /**
     * Returns only the public label info: id, code, name, hasPushNotifications
     */
    fun getByCode(code: String): LabelDto {
        val label =
            dao.getByCode(code) ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with code $code not found" }
        return LabelDto(label.publicId, null, label.code, label.name, label.snsApplicationArn != null)
    }

    fun get(labelId: Int) = mapLabel(dao.getById(labelId)!!)

    fun getList(includeApiKeys: Boolean = false): List<LabelDto> {
        return dao.getList().map { mapLabel(it, includeApiKeys) }
    }

    fun createLabel(dto: LabelDto): LabelDto {
        val label = dao.create(dto)

        if (!dto.fcmApiKey.isNullOrBlank()) {
            val arn = notificationService.createGCMApplication("label-${label.publicId}-fcm", dto.fcmApiKey!!)
            dao.registerApiKey(label.id, dto.fcmApiKey!!, arn)
            return mapLabel(label).copy(hasPushNotifications = true)
        }
        return mapLabel(label)
    }

    fun updateLabel(id: UUID, dto: UpdateLabelDto, actor: User): LabelDto {
        val label = resolveLabel(id, actor)
        val updatedLabel = dao.updateLabel(
            label.id,
            dto.name ?: label.name,
            dto.code ?: label.code,
        )
        return mapLabel(dao.getById(updatedLabel.id)!!)
    }

    fun deleteLabel(id: UUID, actor: User) {
        val label = resolveLabel(id, actor)
        if (!label.snsApplicationArn.isNullOrEmpty()) {
            notificationService.deleteApplication(label.snsApplicationArn)
        }
        dao.delete(label.id)
    }

    fun setupPushNotifications(id: UUID, apiKey: String, actor: User) {
        val label = resolveLabel(id, actor)
        if (label.fcmApiKey == apiKey) return
        if (label.snsApplicationArn != null) {
            notificationService.updateGCMApplication(label.snsApplicationArn, apiKey)
            dao.registerApiKey(label.id, apiKey, label.snsApplicationArn)
        } else {
            val arn = notificationService.createGCMApplication("label-${label.publicId}-fcm", apiKey)
            dao.registerApiKey(label.id, apiKey, arn)
        }
    }

    fun setupPushNotificationText(dto: ConfigurePushNotificationMessageDto, id: UUID, actor: User) {
        val label = resolveLabel(id, actor)
        dao.updatePushNotificationText(label, dto.text!!)
    }

    private fun resolveLabel(labelId: UUID, actor: User): Label {
        val label = dao.getByPublicId(labelId)
        failResponseIf(
            label == null || (actor.role == UserRole.CUSTOMER && actor.labelId != label.id),
            HttpStatus.NOT_FOUND
        ) {
            "Label with id $label not found"
        }
        return label
    }

    private fun mapLabel(label: Label, includeApiKey: Boolean = false) =
        LabelDto(
            label.publicId, databaseUtcToZoned(label.created), label.code, label.name,
            label.snsApplicationArn != null, if (includeApiKey) label.fcmApiKey else null
        )
}
