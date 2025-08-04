package xyz.axie.scheduler

import java.util.concurrent.ScheduledExecutorService

/**
 * The scheduler interface is used to run tasks in different thread pools.
 *
 * There are several implementations of this interface, such as
 * - Coroutine scheduler - runs tasks in the kotlin coroutines context
 * - Executor scheduler - runs tasks using the java [ScheduledExecutorService]
 *
 * You can select type of scheduler by [SchedulerType]
 *
 * Different implementations can be used depending on the requirements of the project.
 * For example, Coroutine scheduler allows to run very big number of tasks in a single thread pool,
 * while Executor scheduler is precise in millisecond's timings (while coroutines are not).
 */
interface Scheduler {

    /**
     * Starts a task with a specified parameters.
     *
     * @param delay Delay for executing a task. `0` - Without delay.
     * @param period Period for a repeating task. `0` - Without repeating.
     * @param task Task to execute.
     *
     * @return Instance of [SchedulerTask].
     */
    fun execute(
        delay: ExecutionTime = ExecutionTime.ZERO,
        period: ExecutionTime = ExecutionTime.ZERO,
        task: suspend SchedulerTask.() -> Unit
    ): SchedulerTask

    /**
     * Gets the task by its id.
     *
     * @param id The task id.
     * @return The task object or null if the task is not found.
     */
    operator fun get(id: Int): SchedulerTask?

    /**
     * Cancels all running tasks.
     */
    fun cancelAllTasks()
}