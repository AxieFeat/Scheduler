# Scheduler

![JitPack](https://img.shields.io/jitpack/version/com.github.AxieFeat/Scheduler)

[English](README.md) | [Русский](README_ru.md)

Its simple library for scheduling tasks on Kotlin via Coroutines and Java executor.

### Requirements
- JDK 21 or higher
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
    // For creation instances of schedulers exist class BasicSchedulerManager, but recommended .scheduler function
    scheduler {} // <- Creates scheduler with default implementation (Default is Coroutines)
    scheduler(SchedulerType.COROUTINES) {} // <- Also creates coroutine scheduler :/
    scheduler(SchedulerType.EXECUTOR) {} // <- Creates scheduler based on Java executor.

    scheduler { // Note that every call of this function creates a new instance of scheduler with own task counter!

        // For execution tasks in scheduler exist function execute, that returns instance of SchedulerTask.


        execute { /* Your code here */ } // This function will immediately execute your code.

        execute(delay = 1.s()) { /* Your code here */ } // This function will execute your task after 1 second.

        // This function will execute your task after 1.5 seconds and then repeat it every hour.
        execute(delay = 1500.ms(), period = 1.h()) { /* Your code here */ }

        // How you can see here used .ms, .h, .s functions for setting a time - here list of all this functions.
        // .ns() -> Nanoseconds
        // .us() -> Microseconds
        // .ms() -> Milliseconds
        // .s() -> Seconds
        // .m() -> Minutes
        // .h() -> Hours
        // .d() -> Days
        // All of this functions are extension for Number class.


        val task = execute {} // You can write the task in variable and, in example, cancel it.


        // Also, while the task is in process of execution, you can cancel it
        var counter = 0
        execute(period = 1500.ms()) {
            counter++
            println(counter)
            if(counter >= 10) {
                this.cancel() // Cancelling the current task.
            }
        }
    }

    // For the reason described above, it is highly recommended
    // to create an instance and store it in a variable for later reuse.
    val someScheduler = scheduler {}

    // Then you can execute tasks via this scheduler.
    someScheduler.execute {}
}
```