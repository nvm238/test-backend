package com.innovattic.medicinfo.logic.dto.migration

import com.innovattic.common.error.createResponseStatusException
import com.innovattic.medicinfo.database.ErrorCodes
import com.innovattic.medicinfo.database.dao.ConversationDao
import com.innovattic.medicinfo.database.dao.UserDao
import com.innovattic.medicinfo.database.dto.UserRole
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class MigrationService(
    private val userDao: UserDao,
    private val conversationDao: ConversationDao
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun migrateUser(dto: MigrateDto, replaceExistingApiKey: Boolean): MigratedDto {

        log.info("Migrating user {} conversation {}", dto.userId, dto.conversationId)
        val user = userDao.getByPublicId(dto.userId!!)
        if (user == null) {
            log.info("Migrating user {} failed: user not found", dto.userId)
            throw createResponseStatusException(
                HttpStatus.BAD_REQUEST,
                code = ErrorCodes.INVALID_MIGRATION
            ) { "User not found" }
        }

        if (userDao.hasApiKey(dto.userId)) {
            if (replaceExistingApiKey) {
                log.info("Deleting existing API key for user (for migration testing)", dto.userId)
                userDao.removeApiKey(dto.userId)
            } else {
                log.info("Migrating user {} failed: already had an api key", dto.userId)
                throw createResponseStatusException(HttpStatus.BAD_REQUEST, code = ErrorCodes.USER_ALREADY_MIGRATED) { "Already migrated" }
            }
        }

        val latestConversationId = conversationDao.getLatest(user.id)?.publicId
        if (latestConversationId != dto.conversationId!! || user.role != UserRole.CUSTOMER) {
            if (latestConversationId != dto.conversationId) {
                log.info("Migrating user {} failed: latest conversation id mismatch", dto.userId)
            } else {
                log.info("Migrating user {} failed: not a customer", dto.userId)
            }
            throw createResponseStatusException(HttpStatus.BAD_REQUEST, code = ErrorCodes.INVALID_MIGRATION) { "Invalid migration" }
        }

        val apiKey = userDao.generateApiKey(user.id)
        log.info("Migrating user {} succeeded: api key generated", dto.userId)
        return MigratedDto(dto.userId, dto.conversationId, apiKey)
    }
}
