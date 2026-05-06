# Code Style

## Return Types

Every function and method **must** declare an explicit return type, **except** when the return type is `Unit` — in that case the return type may be omitted. However, functions that return `Unit` **must** always use a block body (`{ }`), never an expression body (`=`).

```kotlin
// Correct — non-Unit: explicit return type
fun getUser(id: String): User { ... }
fun loadBible(): Flow<List<Book>> { ... }
suspend fun syncData(): Result<Unit> { ... }
fun buildGreeting(): String { ... }

// Correct — Unit: no return type, block body
fun logEvent(name: String) {
    analytics.log(name)
}

// Wrong — non-Unit return type omitted
fun getUser(id: String) = userRepository.find(id)
fun buildGreeting() = "Hello"

// Wrong — Unit function using expression body
fun logEvent(name: String) = analytics.log(name)
```

This applies to:
- Top-level functions
- Class methods
- Extension functions
- Composable functions (return `Unit` — use block body, omit return type)

## Expression Body

Non-Unit functions **must** use expression body (`=`) whenever the entire body is a single expression. Reserve block body (`{ return ... }`) for functions that require multiple statements.

```kotlin
// Correct — single expression, use =
fun getUser(id: String): User = userRepository.find(id)

fun buildGreeting(name: String): String = "Hello, $name"

operator fun invoke(versionId: String): Result<Unit> = try {
    Result.success(repository.fetch(versionId))
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}

// Correct — multiple statements require block body
fun loadAndLog(id: String): User {
    val user = userRepository.find(id)
    Logger.d { "Loaded $user" }
    return user
}

// Wrong — block body with a single return
fun getUser(id: String): User {
    return userRepository.find(id)
}

fun buildGreeting(name: String): String {
    return "Hello, $name"
}
```

This applies to:
- Top-level functions
- Class methods
- Extension functions
- `operator fun invoke`

## Imports

Never use fully-qualified (inline) type or function references in the body of a function or expression. Always add the import at the top of the file and use the simple name.

```kotlin
// Correct — import declared, simple name used in body
import org.koin.core.module.Module

fun KoinApplication.registerModules() {
    Module.bindDefaultBibleVersionDownloaderFacade()
    Module.bindSomethingElse()
}

// Wrong — fully-qualified inline reference
fun KoinApplication.registerModules() {
    org.koin.core.module.Module.bindDefaultBibleVersionDownloaderFacade()
    org.koin.core.module.Module.bindSomethingElse()
}
```

This applies to:
- Class references
- Extension function receivers
- Companion object and static-style calls
- Type aliases and generic bounds
