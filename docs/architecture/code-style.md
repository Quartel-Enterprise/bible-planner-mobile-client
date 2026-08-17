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

## Function Naming

A function that returns a value **must** be named as a **verb phrase**, and the verb **must** say what
kind of operation it is. A bare noun (`avatarImageCropper()`, `pendingFlow()`, `dayParams()`) reads like a
property and hides whether the call creates, reads, computes or does I/O.

| Prefix | Use for |
|---|---|
| *(none — declare a `val`)* | no parameters and no real computation: it is not a function |
| `get…` | cheap in-memory read that takes a parameter |
| `fetch…` / `load…` | I/O — network, disk, database |
| `observe…` | returns a `Flow` you subscribe to |
| `create…` / `build…` | produces a **new** instance |
| `find…` | lookup that may miss (pair with `…OrNull`) |
| `to…` / `as…` | converts between representations |
| `is…` / `has…` / `can…` | returns `Boolean` |

```kotlin
// Correct
expect fun createAvatarImageCropper(): AvatarImageCropper
fun observePending(): Flow<List<E>>
fun getGeneratingCount(excludingKey: String?): Int
val deviceName: String

// Wrong
expect fun avatarImageCropper(): AvatarImageCropper   // creates a new instance — 'get' would lie too
fun pendingFlow(): Flow<List<E>>                      // noun; say what it does with the flow
fun generatingCount(excludingKey: String?): Int       // noun
fun deviceName(): String                              // no params, no computation — should be a val
```

Note that `get` is **not** a catch-all. In Kotlin a parameterless `getX()` should be a property, and
`get` on a factory would claim the instance already exists.

This is enforced automatically by the custom ktlint rule
`bible-planner-style:value-returning-function-naming` (in `tools/ktlint-custom-rules`), which flags any
function with a non-`Unit` return type whose name does not start with an approved verb.

**Exempt** (the rule skips these):
- `@Composable` functions that return a value — the Compose API guidelines deliberately name these as
  nouns (`googleLogo()`, `megabytesText()`, `mainContentBottomInset()`).
- Extension functions on `Modifier` (`Modifier.shimmer()`, `Modifier.verticalScrollbar()`).
- `override` and `actual` declarations — the convention is enforced at the `expect`/interface site.
- `operator` functions, and names ending in `…OrNull` / `…Of` / `…For`.
- Members of a `@Dao` or `@Database` type — Room dictates `bookDao()`, `chapterDao()`, etc.
- Test source sets — fixtures named after what they produce (`versionDto()`, `passage()`, `day()`) read
  well in Given/When/Then, so the rule is disabled there in `.editorconfig`.

New verbs are added to `ALLOWED_VERB_PREFIXES` in the rule when a genuinely new kind of operation shows up.

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

## Naming File-Scoped Constants

A `private val` declared at file (top-level) scope — typically right above the `@Composable` function it sizes/shapes for, e.g. a `Dp`, `Shape`, or `TextUnit` constant — **must** use `lowerCamelCase`, never `PascalCase` or `SCREAMING_SNAKE_CASE`.

```kotlin
// Correct
private val buttonMinHeight = 44.dp

@Composable
internal fun MyButton(...) { ... }

// Wrong
private val ButtonMinHeight = 44.dp
private val BUTTON_MIN_HEIGHT = 44.dp
```

This does **not** apply to:
- Actual constants: `private const val` at companion-object scope stays `UPPER_SNAKE_CASE` (see [dry.md](dry.md) / no-constant-value-params convention).
- Public `CompositionLocal` instances (e.g. `val LocalSnackbarHostState = ...`) and public singleton-like objects (e.g. `val NoClip = object : ...`) — these keep `PascalCase`, following standard Compose/Kotlin convention for object-like values.

This is enforced automatically: the custom ktlint rule `bible-planner-style:private-top-level-val-naming` (in `tools/ktlint-custom-rules`) fails the build on a violation. Stock ktlint's `standard:property-naming` rule permits `PascalCase` for this kind of file-scoped `Dp`/`Color`-typed `val` (it treats them as "constant-like"), which is why a dedicated rule was needed.

## Positioning File-Scoped Constants

Every top-level `private val` or `private const val` **must** be declared before the first top-level function/class/object in the file — i.e. right after the imports — never after the functions that use it. This applies to both the `Dp`/`Shape`/etc. "constant-like" `val`s above and to plain `private const val` magic numbers.

```kotlin
// Correct
import androidx.compose.ui.unit.dp

private val buttonMinHeight = 44.dp
private const val ANIMATION_MILLIS = 300

@Composable
internal fun MyButton(...) { ... }

// Wrong — declared after the function that uses it
import androidx.compose.ui.unit.dp

@Composable
internal fun MyButton(...) { ... }

private val buttonMinHeight = 44.dp
private const val ANIMATION_MILLIS = 300
```

This is enforced automatically by the custom ktlint rule `bible-planner-style:top-level-val-position` (same module as above).

## Blank Lines Between File-Scoped Constants

Consecutive top-level `private val` / `private const val` declarations are written as one block, with **no** blank line between them. The blank line goes only between the block and the first function/class that follows it.

```kotlin
// Correct
private val consequenceSpacing = 10.dp
private val progressIconSize = 44.dp
private val progressSpacing = 18.dp

@Composable
internal fun MyDialog(...) { ... }

// Wrong — blank lines scattering one block of constants
private val consequenceSpacing = 10.dp

private val progressIconSize = 44.dp

private val progressSpacing = 18.dp
```

Enforced by the custom ktlint rule `bible-planner-style:top-level-val-blank-line`. It is scoped to *private* top-level properties — public top-level `val`s (e.g. the generated color palettes in `ui/theme`) may keep blank lines between groups.

## When-Entry Bodies

A `when` entry whose body is a single statement that fits on one line **must not** be wrapped in `{ }` braces.

```kotlin
// Correct
when (status) {
    Status.LOADING -> ProgressIndicator()
    Status.ERROR -> ErrorState()
}

// Wrong
when (status) {
    Status.LOADING -> {
        ProgressIndicator()
    }
    Status.ERROR -> {
        ErrorState()
    }
}
```

If `condition -> statement` would exceed the 120-char line limit, wrap the statement onto its own indented line instead of adding braces:

```kotlin
SessionStatus.NotAuthenticated ->
    stringResource(Res.string.diagnostics_account_not_connected)
```

Braces stay when a branch is genuinely multi-statement, or when the branch's sole statement is itself a bare lambda literal (removing the outer braces there would change meaning — the outer `{ }` is the required block syntax, not optional wrapping).

Because this is the opposite of stock ktlint's `standard:when-entry-bracing` (which forces braces onto *every* entry in a `when` if *any* entry needs them), that stock rule is disabled in `.editorconfig` (`ktlint_standard_when-entry-bracing = disabled`) in favor of the custom rule `bible-planner-style:when-entry-single-statement-braces` (in `tools/ktlint-custom-rules`).

## fun interface

Every Kotlin `interface` with exactly one abstract method (and no abstract properties) **must** be declared as `fun interface` (a functional/SAM interface), so callers can pass a lambda instead of an anonymous object.

```kotlin
// Correct
fun interface GetUserUseCase {
    suspend operator fun invoke(id: String): User
}

// Wrong
interface GetUserUseCase {
    suspend operator fun invoke(id: String): User
}
```

Exception: if the single abstract method has a parameter with a default value, it **cannot** be a `fun interface` (Kotlin forbids default parameter values on a SAM's abstract method) — leave it as a plain `interface`.

Enforced by the custom ktlint rule `bible-planner-style:fun-interface-required`.

### No redundant SAM constructors

When passing a lambda as an argument whose parameter type is a `fun interface`, pass the lambda directly — don't wrap it in the interface's name (the "SAM constructor").

```kotlin
// Correct
someUseCaseConsumer(onResult = { user -> ... })

// Wrong — redundant SAM constructor
someUseCaseConsumer(onResult = GetUserUseCase { user -> ... })
```

Best-effort enforcement by the custom ktlint rule `bible-planner-style:redundant-sam-constructor-argument`. Note this rule can only see fun interfaces declared in the *same file* as the call site (ktlint rules don't do cross-file type resolution), so it won't catch every case — treat it as a net, not a guarantee, when reviewing code that constructs a fun interface from a different module than where it's declared.
