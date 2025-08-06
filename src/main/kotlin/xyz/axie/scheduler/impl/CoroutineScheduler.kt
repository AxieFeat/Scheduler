package xyz.axie.scheduler.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import xyz.axie.scheduler.SchedulerTask
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Creates a [CoroutineScheduler] with a [SupervisorJob] in the provided [CoroutineContext].
 *
 * @param context The [CoroutineContext] to be used for the [CoroutineScope].
 *
 * @return A new instance of [CoroutineScheduler].
 */
inline fun coroutineSupervisor(context: CoroutineContext): CoroutineScheduler =
    CoroutineScheduler(CoroutineScope(context + SupervisorJob() + CoroutineName("CoroutineScheduler")))

/**
 * A [CoroutineScheduler] using the [GlobalScope].
 * This should be used with caution as tasks in the GlobalScope are not bound to any specific lifecycle.
 */
@DelicateCoroutinesApi
inline fun coroutineGlobal(): CoroutineScheduler = CoroutineScheduler(GlobalScope)

class CoroutineScheduler(
    private val scope: CoroutineScope
) : AbstractScheduler(), CoroutineScope by scope {

    override fun execute(delay: Duration, period: Duration, task: suspend SchedulerTask.() -> Unit): SchedulerTask {
        val taskId = nextId()

        lateinit var schedulerTask: SchedulerTask

        val job = when {
            period > Duration.ZERO -> {
                flow {
                    if (delay > Duration.ZERO) delay(delay)
                    emit(Unit)
                    while (true) {
                        delay(period)
                        emit(Unit)
                    }
                }
                    .onEach {
                        scope.launch {
                            try {
                                task(schedulerTask)
                            } catch (t: Throwable) {
                                throw t
                            }
                        }
                    }
                    .onCompletion {}
                    .launchIn(scope)
            }

            delay > Duration.ZERO -> {
                scope.launch {
                    delay(delay)
                    try {
                        task(schedulerTask)
                    } catch (t: Throwable) {
                        throw t
                    }
                }.also {
                    it.invokeOnCompletion { unregisterTask(taskId) }
                }
            }

            else -> {
                scope.launch {
                    try {
                        task(schedulerTask)
                    } catch (t: Throwable) {
                        throw t
                    }
                    unregisterTask(taskId)
                }
            }
        }

        return newTask(taskId, job).also { schedulerTask = it }
    }

    private fun newTask(id: Int, job: Job): SchedulerTask {
        val task = CoroutineSchedulerTask(this, id, job)

        registerTask(task)
        return task
    }

    class CoroutineSchedulerTask(
        override val scheduler: AbstractScheduler,
        override val id: Int,
        private val job: Job
    ) : SchedulerTask {

        override val cancelled: Boolean
            get() = job.isCancelled

        override fun cancel() {
            try {
                job.cancel()
            } finally {
                scheduler.unregisterTask(id)
            }
        }
    }
}