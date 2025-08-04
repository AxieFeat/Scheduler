package xyz.axie.scheduler.impl

import xyz.axie.scheduler.Scheduler
import xyz.axie.scheduler.SchedulerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.DurationUnit

abstract class AbstractScheduler : Scheduler {

    private val tasks = ConcurrentHashMap<Int, SchedulerTask>()

    private var tasksWas = AtomicInteger(0)

    override fun execute(
        delay: Duration,
        period: Duration,
        task: suspend SchedulerTask.() -> Unit
    ): SchedulerTask = this.execute(
        delay.toLong(DurationUnit.MILLISECONDS),
        period.toLong(DurationUnit.MILLISECONDS),
        task
    )

    abstract fun execute(
        delay: Long,
        period: Long,
        task: suspend SchedulerTask.() -> Unit
    ): SchedulerTask

    protected fun nextId(): Int {
        return tasksWas.incrementAndGet()
    }

    internal fun registerTask(task: SchedulerTask) {
        tasks[task.id] = task
    }

    internal fun unregisterTask(taskId: Int) {
        tasks.remove(taskId)
    }

    override operator fun get(id: Int): SchedulerTask? {
        return tasks[id]
    }

    override fun cancelAllTasks() {
        tasks.values.forEach(SchedulerTask::cancel)
        tasks.clear()
    }

}