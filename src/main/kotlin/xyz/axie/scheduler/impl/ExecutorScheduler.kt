package xyz.axie.scheduler.impl

import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import xyz.axie.scheduler.SchedulerTask
import java.util.concurrent.ScheduledExecutorService
import kotlin.time.Duration

/**
 * Creates an [ExecutorScheduler] with a thread pool of the specified size.
 *
 * @param threadCount The number of threads in the pool.
 *
 * @return A new instance of [ExecutorScheduler].
 */
inline fun executorThreadPool(threadCount: Int): ExecutorScheduler = ExecutorScheduler(threadCount)

/**
 * Creates an [ExecutorScheduler] with a single-threaded executor.
 *
 * @return A new instance of [ExecutorScheduler].
 */
inline fun executorSingleThread(): ExecutorScheduler = ExecutorScheduler()

class ExecutorScheduler(
    private val executor: ScheduledExecutorService
) : AbstractScheduler() {

    constructor(threadCount: Int) : this(Executors.newScheduledThreadPool(threadCount))

    constructor() : this(Executors.newSingleThreadScheduledExecutor())

    override fun execute(
        delay: Duration,
        period: Duration,
        task: suspend SchedulerTask.() -> Unit
    ): SchedulerTask = this.execute(
        delay.inWholeNanoseconds,
        period.inWholeNanoseconds,
        task
    )

    fun execute(delay: Long, period: Long, task: suspend SchedulerTask.() -> Unit): SchedulerTask {
        val taskId = nextId()

        lateinit var schedulerTask: SchedulerTask

        val wrapped = Runnable {
            try {
                runBlocking {
                    task.invoke(schedulerTask)
                }
            } catch (throwable: Throwable) {
                throw throwable
            } finally {
                if (period == -1L) {
                    unregisterTask(taskId)
                }
            }
        }

        val future = when {
            period > 0L -> {
                executor.scheduleAtFixedRate(wrapped, delay, period, TimeUnit.NANOSECONDS)
            }
            delay > 0L -> {
                executor.schedule(wrapped, delay, TimeUnit.NANOSECONDS)
            }
            else -> {
                executor.submit(wrapped)
            }
        }

        return newTask(taskId, future).also { schedulerTask = it }
    }

    private fun newTask(id: Int, future: Future<*>): SchedulerTask {
        val task = ExecutorSchedulerTask(this, id, future)
        registerTask(task)
        return task
    }

    class ExecutorSchedulerTask(
        override val scheduler: AbstractScheduler,
        override val id: Int,
        private val future: Future<*>
    ) : SchedulerTask {

        override val cancelled: Boolean
            get() = future.isCancelled

        override fun cancel() {
            try {
                future.cancel(false)
            } finally {
                scheduler.unregisterTask(id)
            }
        }
    }
}