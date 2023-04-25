package com.innovattic.medicinfo.logic

import com.innovattic.medicinfo.logic.dto.HeartBeatDto
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.ZonedDateTime

@Component
class HeartBeatService(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val clock: Clock
) {

    @Scheduled(fixedDelay = 10000L)
    fun schedule() {
        sendHeartBeat()
    }

    fun sendHeartBeat() {
        simpMessagingTemplate.convertAndSend(HEARTBEAT_TOPIC_ROUTE, HeartBeatDto(ZonedDateTime.now(clock)))
    }

    companion object {
        const val HEARTBEAT_TOPIC_ROUTE = "/v1/topic/heartbeat"
    }
}
