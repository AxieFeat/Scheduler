# Scheduler

[![](https://jitpack.io/v/AxieFeat/Scheduler.svg)](https://jitpack.io/#AxieFeat/Scheduler)

[English](README.md) | [Русский](README_ru.md)

Its simple library for scheduling tasks on Kotlin via Coroutines and Java executor.

### Requirements
- JDK 8 or higher
- Kotlin

### Setup

1. Specify the Jitpack repository:
```kotlin
repositories {
    maven("https://jitpack.io")
}
```

2. Add the dependency to your project:
```kotlin
dependencies {
    implementation("com.github.AxieFeat:Scheduler:<last version>") // See last version in top of README
}
```

### Basic Usage Example
Here's a simple example of using scheduler:

```kotlin
fun main() {
    // This scheduler has two implementations - Coroutines and Java executor.

    // Example of creation coroutine-based scheduler.
    val coroutineScheduler = CoroutineScheduler(GlobalScope)

    // Example of creation java executor-based scheduler.
    val executorScheduler = ExecutorScheduler(Executors.newSingleThreadScheduledExecutor())

    // It is very important to understand that each scheduler instance is independent and has its own task counter.

    // There are functions for faster creation:
    // .coroutineSupervisor, .coroutineGlobal - For coroutine-based scheduler.
    // .executorThreadPool, .executorSingleThread - For java executor-based scheduler.

    val anotherCoroutineScheduler = coroutineGlobal() // Equals `CoroutineScheduler(GlobalScope)`
    val anotherExecutorScheduler = executorSingleThread() // Equals `ExecutorScheduler(Executors.newSingleThreadScheduledExecutor())`

    // Then you can call .execute functions:
    anotherCoroutineScheduler.execute {}
    anotherExecutorScheduler.apply {
        this.execute {}
    }

    // But also exist .scheduler function, it simply calls .apply on the object.
    scheduler(coroutineGlobal()) {
        execute {}
    }
    scheduler(anotherExecutorScheduler) {
        execute {}
    }

    // Something about .execute function
    scheduler(executorThreadPool(2)) {

        // For execution tasks in scheduler exist function execute, that returns instance of SchedulerTask.


        execute { /* Your code here */ } // This function will immediately execute your code.

        execute(delay = 1.seconds) { /* Your code here */ } // This function will execute your task after 1 second.

        // This function will execute your task after 1.5 seconds and then repeat it every hour.
        execute(delay = 1500.milliseconds, period = 1.hours) { /* Your code here */ }

        val task = execute {} // You can write the task in variable and, in example, cancel it in the future.

        // Also, while the task is in process of execution, you can cancel it
        var counter = 0
        execute(period = 1500.milliseconds) {
            counter++
            println(counter)
            if(counter >= 10) {
                this.cancel() // Cancelling the current task.
            }
        }
    }
}
```