package com.innovattic.medicinfo.logic

import com.fasterxml.jackson.databind.ObjectMapper
import com.innovattic.medicinfo.database.dao.UserAppSelfTestResultDao
import com.innovattic.medicinfo.logic.dto.SelfTestAnswersDto
import org.springframework.stereotype.Component

@Component
class UserSelfTestResultService(
    private val dao: UserAppSelfTestResultDao,
    private val objectMapper: ObjectMapper
) {

    fun createOrUpdate(userId: Int, labelId: Int, dto: SelfTestAnswersDto) {
        val results = dao.get(userId)
        val result = objectMapper.writeValueAsString(dto)
        if (results != null) {
            dao.update(results.id, result)
        } else {
            dao.create(userId, labelId, result)
        }
    }

    fun get(userId: Int): SelfTestAnswersDto? {
        return dao.get(userId)?.data?.let { data -> objectMapper.readValue(data, SelfTestAnswersDto::class.java) }
    }
}
