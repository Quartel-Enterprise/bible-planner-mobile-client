# Coroutine Error Handling

## The Problem

Kotlin's `CancellationException` extends `Exception`. Any generic `catch (e: Exception)` block will silently swallow it, breaking structured concurrency — the coroutine appears cancelled to the caller but its body keeps running.

```kotlin
// Wrong — cancellation is swallowed
suspend fun invoke(): Result<Unit> = try {
    Result.success(doWork())
} catch (e: Exception) {
    Result.failure(e)
}

// Wrong — verbose but still repeated boilerplate
suspend fun invoke(): Result<Unit> = try {
    Result.success(doWork())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
```

## The Solution: `suspendRunCatching`

Use `suspendRunCatching` from `core/utils` whenever you need to wrap a suspend call in a `Result` at the top level of a `suspend fun`:

```kotlin
import com.quare.bibleplanner.core.utils.suspendRunCatching

suspend fun invoke(id: String): Result<Unit> = suspendRunCatching {
    doWork(id)
}
```

It re-throws `CancellationException` and wraps everything else in `Result.failure`.

### Chaining `.onFailure` for logging

When you need to log the error before returning, chain `.onFailure` — do not revert to a manual try/catch:

```kotlin
suspend fun invoke(versionId: String, bookId: BookId): Result<Unit> =
    suspendRunCatching {
        doHeavyWork(versionId, bookId)
    }.onFailure { Logger.e { "Failed for $bookId: ${it.message}" } }
```

## Where to use it

Apply `suspendRunCatching` at the outermost boundary of a `suspend fun` that returns `Result<T>`. This is typically:

- `operator fun invoke(...)` in use cases
- Repository functions that return `Result<T>`
- Any other `suspend fun` that needs to map exceptions to `Result`

## Where NOT to use it

`suspendRunCatching` is an `inline` function. The Kotlin compiler forbids calling `inline` functions inside lambdas passed to non-inline functions. This means you **cannot** use it inside `launch { }`, `async { }`, `forEach { }`, or similar blocks:

```kotlin
// Wrong — inline function called inside a non-inline lambda
launch {
    suspendRunCatching { downloadChapter() } // compile error
}

// Correct — keep the explicit pattern inside launch/async bodies
launch {
    try {
        downloadChapter()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Logger.e { "Error: ${e.message}" }
    }
}
```

For coroutine body error handling that only needs to log and continue (not return a `Result`), the explicit pattern is the right tool.

## Do NOT use stdlib `runCatching`

`kotlin.runCatching` does **not** re-throw `CancellationException`. Never use it in suspend contexts:

```kotlin
// Wrong — swallows CancellationException
suspend fun invoke(): Result<Unit> = runCatching { doWork() }

// Correct
suspend fun invoke(): Result<Unit> = suspendRunCatching { doWork() }
```
