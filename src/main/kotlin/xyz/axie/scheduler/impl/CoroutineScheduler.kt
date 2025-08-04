package xyz.axie.scheduler.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import xyz.axie.scheduler.Scheduler
import xyz.axie.scheduler.SchedulerTask

class CoroutineScheduler : AbstractScheduler() {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun Scheduler.execute(delay: Long, period: Long, task: suspend SchedulerTask.() -> Unit): SchedulerTask {
        val taskId = nextId()

        lateinit var schedulerTask: SchedulerTask

        val job = when {
            period > 0L -> {
                flow {
                    if (delay > 0L) delay(delay)
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

            delay > 0L -> {
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