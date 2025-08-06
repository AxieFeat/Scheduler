# Scheduler

[![](https://jitpack.io/v/AxieFeat/Scheduler.svg)](https://jitpack.io/#AxieFeat/Scheduler)

[English](README.md) | [Русский](README_ru.md)

Это очень простая библиотека для выполнения задач на основе Coroutines и Java executor.

### Требования
- JDK 8 или выше
- Kotlin

### Установка

1. Укажите репозиторий Jitpack:
```kotlin
repositories {
    maven("https://jitpack.io")
}
```

2. Добавьте зависимость в ваш проект:
```kotlin
dependencies {
    implementation("com.github.AxieFeat:Scheduler:<last version>") // Посмотрите последний релиз в начале README
}
```

### Просто пример использование
Здесь представлен простой пример использования:

```kotlin
fun main() {
    // У этого планировщика есть две реализации - Coroutines и Java executor.

    // Пример создания планировщика на основе корутин.
    val coroutineScheduler = CoroutineScheduler(GlobalScope)

    // Пример создания планировщика на основе Java executor.
    val executorScheduler = ExecutorScheduler(Executors.newSingleThreadScheduledExecutor())

    // Так же очень важно понимать, что каждая инстанса планировщика независима и имеет свой личный счётчик задач.

    // Существуют функции для быстрого создания планировщиков:
    // .coroutineSupervisor, .coroutineGlobal - Для планировщика на корутинах.
    // .executorThreadPool, .executorSingleThread - Для планировщика на Java executor.

    val anotherCoroutineScheduler = coroutineGlobal() // Эквивалент `CoroutineScheduler(GlobalScope)`
    val anotherExecutorScheduler = executorSingleThread() // Эквивалент `ExecutorScheduler(Executors.newSingleThreadScheduledExecutor())`

    // Затем вы можете вызывать функцию .execute:
    anotherCoroutineScheduler.execute {}
    anotherExecutorScheduler.apply {
        this.execute {}
    }

    // Но так же существует функция .scheduler, она просто вызывает функция .apply на объекте.
    scheduler(coroutineGlobal()) {
        execute {}
    }
    scheduler(anotherExecutorScheduler) {
        execute {}
    }

    // Немного о функции .execute
    scheduler(executorThreadPool(2)) {

        // Для выполнения задач существует функция execute, которая возвращает экземпляр SchedulerTask.


        execute { /* Ваш код здесь */ } // Эта функция немедленно выполнит ваш код.

        execute(delay = 1.seconds) { /* Ваш код здесь */ } // Эта функция выполнит ваш код через 1 секунду.

        // Эта функция выполнит ваш код через 1.5 секунды, а затем будет выполнять его каждый час.
        execute(delay = 1500.milliseconds, period = 1.hours) { /* Ваш код здесь */ }

        val task = execute {} // Вы можете сохранить задачу в переменную и, например, отменить её в будущем.

        // Так же вы можете отменить задачу во время её выполнения.
        var counter = 0
        execute(period = 1500.milliseconds) {
            counter++
            println(counter)
            if(counter >= 10) {
                this.cancel() // Отмена текущей задачи.
            }
        }
    }
}
```