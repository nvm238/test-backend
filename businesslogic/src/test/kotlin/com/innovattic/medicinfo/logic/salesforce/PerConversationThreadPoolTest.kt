package com.innovattic.medicinfo.logic.salesforce

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Semaphore

internal class PerConversationThreadPoolTest {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Simple condition abstraction based on a semaphore
    // This is a singe-shot event; don't try to call [set] or [waitFor] multiple times.
    class Condition {
        // when semaphore is *released*, the condition is 'set'
        private val semaphore = Semaphore(0)
        fun set() {
            semaphore.release()
        }
        fun isSet(): Boolean {
            return semaphore.availablePermits() > 0
        }
        fun waitFor() {
            semaphore.acquire()
        }
    }

    /**
     * log with thread name
     */
    private fun log(msg: String) {
        logger.info("Thread ${Thread.currentThread().name}: $msg")
    }

    @Test
    fun `per-conversation execution order`() {

        var failed = false
        val w1FinishedEvent = Condition()
        val testFinishedEvent = Condition()

        val w1 = Runnable {
            log("Starting w1")
            log("Finishing w1")
            w1FinishedEvent.set()
        }
        val w2 = Runnable {
            log("Starting w2")
            if (!w1FinishedEvent.isSet()) {
                failed = true
            }
            log("Finishing w2")
            testFinishedEvent.set()
        }

        val queue = PerConversationThreadPool()
        queue.start()
        val conversationId = UUID.randomUUID()
        queue.addEvent(w1, conversationId)
        queue.addEvent(w2, conversationId)

        testFinishedEvent.waitFor()
        assertFalse(failed)
    }

    @Test
    fun `inter-conversation execution order`() {

        var failed = false
        val w1Completed = Condition()
        val w2Completed = Condition()
        val testFinished = Condition()

        val w1 = Runnable {
            log("Starting w1")
            // just keep running until w2 completes
            // this tests sure that w1 and w2 are executed in parallel
            w2Completed.waitFor()
            log("Finishing w1")
            w1Completed.set()
        }
        val w2 = Runnable {
            log("Starting w2")
            Thread.sleep(50)
            log("Finishing w2")
            w2Completed.set()
        }
        val w3 = Runnable {
            log("Starting w3")
            // This should only be executed when w1 completes
            if (!w1Completed.isSet()) {
                failed = true
            }
            log("Finishing w3")
            testFinished.set()
        }

        val queue = PerConversationThreadPool()
        queue.start()
        val conversationId1 = UUID.randomUUID()
        val conversationId2 = UUID.randomUUID()
        queue.addEvent(w1, conversationId1)
        queue.addEvent(w2, conversationId2)
        queue.addEvent(w3, conversationId1)

        testFinished.waitFor()

        assertFalse(failed)
    }
}
