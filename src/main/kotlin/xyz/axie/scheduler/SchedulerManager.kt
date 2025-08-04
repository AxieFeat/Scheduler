package xyz.axie.scheduler

/**
 * Class for manage schedulers.
 */
interface SchedulerManager {

    /**
     * Create instance of [Scheduler]
     *
     * @param type Scheduler type.
     *
     * @return Instance of [Scheduler]
     */
    fun create(type: SchedulerType): Scheduler
}