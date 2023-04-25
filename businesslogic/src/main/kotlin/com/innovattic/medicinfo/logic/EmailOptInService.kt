package com.innovattic.medicinfo.logic

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.dao.LabelDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.logic.dto.EmailOptInDto
import com.innovattic.medicinfo.logic.eloqua.EloquaApiClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.util.*

@Component
class EmailOptInService(
    private val labelDao: LabelDao,
    private val userDao: UserDao,
    private val eloquaApiClients: Map<String, EloquaApiClient>
) {

    fun sendEmailOptIn(userId: String, dto: EmailOptInDto) {
        val user = userDao.getByPublicId(parseUUIDorThrow(userId))
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "User with uuid: $userId not found" }
        val label = labelDao.getById(user.labelId)
            ?: throw createResponseStatusException(HttpStatus.NOT_FOUND) { "Label with id ${user.labelId} not found" }

        val eloquaApiClient =
            eloquaApiClients[label.code] ?: throw createResponseStatusException(HttpStatus.BAD_REQUEST) {
                "Email opt in not supported for label with code ${label.code}"
            }

        eloquaApiClient.sendEmailOptIn(dto.email)
    }

    private fun parseUUIDorThrow(uuid: String): UUID {
        return try {
            UUID.fromString(uuid)
        } catch (ignoreException: IllegalArgumentException) {
            throw createResponseStatusException(HttpStatus.BAD_REQUEST) { "Malformed UUID: $uuid" }
        }
    }
}
