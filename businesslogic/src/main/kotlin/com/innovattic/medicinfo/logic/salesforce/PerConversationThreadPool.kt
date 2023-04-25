package com.innovattic.medicinfo.logic.salesforce

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * A ThreadPool wrapper that will make sure:
 *
 * (a) all events for one conversation id are executed serially (and in order)
 * (b) events for different conversations can be executed in parallel
 *
 * The selection of effects is not very efficient, so this class should not be used
 * when throughput is critical.
 */
class PerConversationThreadPool {

    companion object {
        const val THREAD_POOL_SIZE = 20

        // max events in the queue before refusing new items
        const val MAX_QUEUE_SIZE = 50000
    }

    private val logger = LoggerFactory.getLogger(javaClass)!!
    private var quit = false
    private var threadPool: ScheduledThreadPoolExecutor? = null

    /**
     * Would be ideal to use a BlockingLinkedList here, however its iterator.remove() is
     * linear, not O(1) !
     */
    private var workQueue = LinkedList<WorkUnit>()
    private val conversationsWithOngoingWork = HashSet<UUID>()

    private data class WorkUnit(val runnable: Runnable, val conversationId: UUID)

    /**
     * Run event. [checkAndMarkInUse] must be called before.
     *
     * After finishing the event, will schedule a next event if another event is
     * runnable.
     *
     * @param work
     */
    private fun processEventAndScheduleNext(work: WorkUnit) {
        if (!conversationsWithOngoingWork.contains(work.conversationId)) {
            logger.error("Logic error; conversation should have been added to conversationsWithOngoingWork")
        }

        @Suppress("TooGenericExceptionCaught") try {
            work.runnable.run()
        } catch (e: Throwable) {
            logger.warn("Exception while processing event", e)
        }

        // Unmark this conversation as ongoing: next work may be scheduled.
        synchronized(conversationsWithOngoingWork) {
            conversationsWithOngoingWork.remove(work.conversationId)
        }
        scheduleWorkIfPossible()
    }

    private fun scheduleWorkIfPossible() {

        // Find any event that is eligible for processing.
        // Make sure the workQueue doesn't get changed while we're iterating it.
        synchronized(workQueue) {

            // Optimization: keep a list of conversations for which we can't execute work
            // (with some refactoring and extra locking, we might also be able to
            // re-use the [conversationsWithOngoingWork] variable here)
            val ineligibleConversations = HashSet<UUID>()

            for (idx in workQueue.indices) {
                val nextWorkUnit = workQueue[idx]
                // Did we already conclude that we can't do work for this conversation?
                if (ineligibleConversations.contains(nextWorkUnit.conversationId)) {
                    continue
                }

                // Execute on the appropriate event queue.
                if (checkAndMarkInUse(nextWorkUnit.conversationId)) {
                    // This conversation can process events
                    schedule(nextWorkUnit)
                    workQueue.removeAt(idx)
                    break
                } else {
                    // This conversation is currently execution another call.
                    // Don't execute any other events for this conversation.
                    ineligibleConversations.add(nextWorkUnit.conversationId)
                }
            }
        }
    }

    /**
     * Check if we can do work for this conversation - ie. there is no other
     * work being done at the moment. When this method returns true,
     * a lock has been set so that no other thread will get true for the same conversation
     * until the lock has been unset.
     *
     * Thread-safe
     */
    private fun checkAndMarkInUse(conversationId: UUID): Boolean {
        synchronized(conversationsWithOngoingWork) {
            if (conversationsWithOngoingWork.contains(conversationId)) {
                return false
            } else {
                conversationsWithOngoingWork.add(conversationId)
                return true
            }
        }
    }

    /**
     * Schedule event for direct execution in any of the worker threads.
     * @param event
     */
    private fun schedule(event: WorkUnit) {
        val runnable = Runnable { processEventAndScheduleNext(event) }
        threadPool!!.schedule(runnable, 0, TimeUnit.MICROSECONDS)
    }

    /**
     * Add work to the queue. If the queue is full, the work will be discarded.
     */
    fun addEvent(runnable: Runnable, conversationId: UUID) {
        if (quit) {
            error("Threadpool is shutting down")
        }

        val event = WorkUnit(runnable, conversationId)
        synchronized(workQueue) {

            if (workQueue.size >= MAX_QUEUE_SIZE) {
                error("Event queue is full; refusing event")
            }

            workQueue.add(event)

            // See if we can schedule it right away
            scheduleWorkIfPossible()
        }
    }

    /**
     * If a runnable does not have an conversationId it can be directly put in the threadpool
     */
    fun addNonConversationEvent(runnable: Runnable) {
        if (quit) {
            error("Threadpool is shutting down")
        }
        threadPool!!.schedule(runnable, 0, TimeUnit.MICROSECONDS)
    }

    /**
     * Start the thread pool.
     */
    fun start() {
        quit = false
        threadPool = ScheduledThreadPoolExecutor(
            THREAD_POOL_SIZE, Executors.defaultThreadFactory()
        )
    }

    /**
     * Shutdown, doesn't wait for all threads to complete.
     */
    fun stop() {
        quit = true
    }

    /**
     * Return queue size of pool
     */
    val queueSize: Int
        get() {
            synchronized(workQueue) {
                return workQueue.size
            }
        }
}
