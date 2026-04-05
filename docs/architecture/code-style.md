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
