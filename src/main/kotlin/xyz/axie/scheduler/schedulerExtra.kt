package xyz.axie.scheduler

/**
 * Applies given [init] to [scheduler].
 *
 * @return Given [scheduler].
 */
inline fun <T> scheduler(scheduler: T, init: T.() -> Unit): T = scheduler.apply(init)