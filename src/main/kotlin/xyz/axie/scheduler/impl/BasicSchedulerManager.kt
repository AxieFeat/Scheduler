package xyz.axie.scheduler.impl

import xyz.axie.scheduler.Scheduler
import xyz.axie.scheduler.SchedulerManager
import xyz.axie.scheduler.SchedulerType

@DslMarker
private annotation class SchedulerDsl

/**
 * New instance of [Scheduler].
 *
 * @param init Scheduler params.
 *
 * @return Instance of [Scheduler].
 */
@SchedulerDsl
fun scheduler(type: SchedulerType = SchedulerType.COROUTINES, init: Scheduler.() -> Unit): Scheduler {
    val cloudScheduler = BasicSchedulerManager.create(type)
    cloudScheduler.init()

    return cloudScheduler
}

object BasicSchedulerManager : SchedulerManager {
    override fun create(type: SchedulerType): Scheduler {
        return when(type) {
            SchedulerType.COROUTINES -> CoroutineScheduler()
            SchedulerType.EXECUTOR -> ExecutorScheduler()
        }
    }
}