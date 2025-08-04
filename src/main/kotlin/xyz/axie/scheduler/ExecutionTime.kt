package xyz.axie.scheduler

import java.util.concurrent.TimeUnit

fun Number.ns() = ExecutionTime(this.toLong(), TimeUnit.NANOSECONDS)
fun Number.us() = ExecutionTime(this.toLong(), TimeUnit.MICROSECONDS)
fun Number.ms() = ExecutionTime(this.toLong(), TimeUnit.MILLISECONDS)
fun Number.s() = ExecutionTime(this.toLong(), TimeUnit.SECONDS)
fun Number.m() = ExecutionTime(this.toLong(), TimeUnit.MINUTES)
fun Number.h() = ExecutionTime(this.toLong(), TimeUnit.HOURS)
fun Number.d() = ExecutionTime(this.toLong(), TimeUnit.DAYS)

/**
 * This class wraps time for executing a task.
 */
class ExecutionTime(
    val value: Long,
    val unit: TimeUnit
) {

    /**
     * Convert time to millis.
     */
    fun toMillis(): Long {
        return unit.toMillis(value)
    }

    companion object {

        /**
         * Constant with zero time.
         */
        @JvmField
        val ZERO = ExecutionTime(0, TimeUnit.MILLISECONDS)
    }
}