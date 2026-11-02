# DRY (Don't Repeat Yourself)

Duplicated logic is a maintenance liability. When logic changes, every copy must change — and one is always missed. Apply DRY whenever the same intent appears more than once.

---

## Repeated conditional logic

When the same condition is evaluated in multiple places, extract it into a named function or property. The name also documents the intent.

```kotlin
// Wrong — same condition duplicated across the call site
fun onEvent(event: UiEvent) {
    when (event) {
        is UiEvent.OnSave -> {
            if (user != null && user.isActive && user.plan != null) saveChanges()
        }
        is UiEvent.OnExport -> {
            if (user != null && user.isActive && user.plan != null) exportData()
        }
    }
}

// Correct — condition extracted and named
private val canPerformActions: Boolean
    get() = user != null && user.isActive && user.plan != null

fun onEvent(event: UiEvent) {
    when (event) {
        is UiEvent.OnSave -> if (canPerformActions) saveChanges()
        is UiEvent.OnExport -> if (canPerformActions) exportData()
    }
}
```

---

## Repeated mapping from domain to UI model

If multiple places transform the same domain type into a UI type, extract a mapper. Inline transformations scattered across the codebase diverge silently.

```kotlin
// Wrong — same mapping written twice in different files
// In ScreenA:
val items = books.map { book ->
    BookUiItem(
        id = book.id,
        title = book.name.uppercase(),
        subtitle = "${book.chapterCount} chapters",
    )
}

// In ScreenB:
val items = books.map { book ->
    BookUiItem(
        id = book.id,
        title = book.name.uppercase(),
        subtitle = "${book.chapterCount} chapters",
    )
}

// Correct — single mapping function, referenced from both places
fun Book.toUiItem(): BookUiItem = BookUiItem(
    id = id,
    title = name.uppercase(),
    subtitle = "$chapterCount chapters",
)
```

---

## Repeated sealed class exhaustion

When a `when` over the same sealed class appears in multiple places with overlapping branches, extract the repeated cases into a shared function.

```kotlin
// Wrong — the Loading and Error branches are identical in two composables
@Composable
fun ScreenA(state: UiState) {
    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(state.message)
        is UiState.Success -> SuccessContentA(state.data)
    }
}

@Composable
fun ScreenB(state: UiState) {
    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(state.message)
        is UiState.Success -> SuccessContentB(state.data)
    }
}

// Correct — shared states handled once, unique states passed as a lambda
@Composable
fun WithUiState(
    state: UiState,
    onSuccess: @Composable (UiState.Success) -> Unit,
) {
    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(state.message)
        is UiState.Success -> onSuccess(state)
    }
}
```

---

## Repeated coroutine launch boilerplate in a ViewModel

When every event handler opens a coroutine and forwards to a private function, the `viewModelScope.launch` is noise. Extract a helper or unify the pattern.

```kotlin
// Wrong — viewModelScope.launch repeated for every action
fun onEvent(event: UiEvent) {
    when (event) {
        is UiEvent.OnSave -> viewModelScope.launch { save(event.data) }
        is UiEvent.OnDelete -> viewModelScope.launch { delete(event.id) }
        is UiEvent.OnRefresh -> viewModelScope.launch { refresh() }
    }
}

// Correct — launch extracted once
fun onEvent(event: UiEvent) {
    when (event) {
        is UiEvent.OnSave -> launch { save(event.data) }
        is UiEvent.OnDelete -> launch { delete(event.id) }
        is UiEvent.OnRefresh -> launch { refresh() }
    }
}

private fun launch(block: suspend () -> Unit) {
    viewModelScope.launch { block() }
}
```

---

## Repeated default arguments at every call site

When the same argument is passed explicitly with the same value everywhere a function is called, make it a default parameter. The exception is when callers should remain conscious of the value.

```kotlin
// Wrong — animated = true repeated at every call site
navigate(route = HomeRoute, animated = true)
navigate(route = ProfileRoute, animated = true)
navigate(route = SettingsRoute, animated = true)

// Correct — true is the expected default; callers opt out explicitly
fun navigate(route: Route, animated: Boolean = true) { ... }

navigate(HomeRoute)
navigate(ProfileRoute)
navigate(SettingsRoute)
navigate(LegacyRoute, animated = false) // exception is visible
```

---

## Factory methods that share structure

Extract a private builder when multiple public methods differ only in a few arguments.

```kotlin
// Wrong — setAutoCancel + PRIORITY_DEFAULT repeated in every result notification
fun createComplete(versionName: String): Notification = getBaseBuilder()
    .setContentTitle(stringProvider.getCompleteTitle(versionName))
    .setContentText(stringProvider.getCompleteMessage())
    .setSmallIcon(android.R.drawable.stat_sys_download_done)
    .setAutoCancel(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .build()

fun createError(versionName: String): Notification = getBaseBuilder()
    .setContentTitle(stringProvider.getErrorTitle(versionName))
    .setContentText(stringProvider.getErrorMessage())
    .setSmallIcon(android.R.drawable.stat_notify_error)
    .setAutoCancel(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .build()

// Correct — shared structure extracted once
fun createComplete(versionName: String): Notification = buildResultNotification(
    title = stringProvider.getCompleteTitle(versionName),
    message = stringProvider.getCompleteMessage(),
    icon = android.R.drawable.stat_sys_download_done,
)

fun createError(versionName: String): Notification = buildResultNotification(
    title = stringProvider.getErrorTitle(versionName),
    message = stringProvider.getErrorMessage(),
    icon = android.R.drawable.stat_notify_error,
)

private fun buildResultNotification(title: String, message: String, icon: Int): Notification =
    getBaseBuilder()
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(icon)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
```

---

## Flows called more than once from the same source

Never call a factory or use case multiple times to obtain the same logical stream. Cache the result.

```kotlin
// Wrong — create() called twice; two independent subscriptions,
// and one result is never exposed
val uiState: StateFlow<UiState> = factory.create().stateIn(scope = viewModelScope, ...)

init {
    observe(factory.create()) { newState ->
        _uiState.update { newState }
    }
}

// Correct — one call, one StateFlow, single source of truth
val uiState: StateFlow<UiState> = factory.create().stateIn(scope = viewModelScope, ...)
```

---

## When NOT to apply DRY

Do not create abstractions for coincidental similarity. Two things that look the same today but represent different concepts should stay separate — forced unification makes future divergence painful.

```kotlin
// Wrong — "paused" and "error" happen to share structure today,
// but they are different concepts that will diverge
private fun buildAnyNotification(title: String, message: String): Notification = ...

// Correct — keep them separate; divergence stays local
fun buildPausedNotification(...): Notification { ... }
fun buildErrorNotification(...): Notification { ... }
```

The rule: extract when the duplication is **structural and intentional**. Leave it when the similarity is **accidental or temporary**.

---

## Enforcement

[jscpd](https://github.com/kucherenko/jscpd) looks for copied code in the production Kotlin and Swift of the apps:
clones of 70 tokens or more with at least 5 lines of code once the package, imports and comments are left out.

```bash
./scripts/cpd.sh                                # check (what CI runs)
./scripts/cpd.sh --update-baseline              # write the copied code found now to .jscpd-baseline.json
./scripts/cpd.sh --update-baseline --allow-new  # also take more copied code in, only when the copy is on purpose
```

`.jscpd.json` says what it scans: no test source sets, `:testing` modules, Gradle scripts nor build outputs. jscpd only
compares files of the same language, so a screen drawn by Compose and by SwiftUI is never a clone. The check itself is
shared with the other Quare apps in [copied-code-check](https://github.com/Quartel-Enterprise/copied-code-check):
`scripts/cpd.sh` is its wrapper, which never changes, and runs the release that the
`Quartel-Enterprise/copied-code-check@X.Y.Z` line of `duplication.yml` pins (every workflow that uses the action has to
pin the same one), with the jscpd release it locks. It needs Node.js 18 or later, plus `curl`, `tar` and access to
github.com the first time it runs a release. A change to how the check counts goes to that repository, then a bump of
the line here (Dependabot proposes it).

`.jscpd-baseline.json` holds how many lines of code of each file are copied today. Lines of a file, not clones: with the
same code in three files, which pairs jscpd reports depends on the order it reads them, and an edit inside a long clone
can split it in two. A file with more lines than the baseline lists fails the check: extract the copied code into one
place the callers share, following the sections above (a use case or coordinator under the ViewModels, a mapper, a
component of `:ui:component`). A file with fewer fails too, until `--update-baseline` writes the new numbers, so the
baseline only shrinks and removed copies can't come back unnoticed. Without `--allow-new` it refuses any file whose
copied lines grew, even when copies removed elsewhere keep the total down, and only takes a moved file: a new path with
the name (the part before the first dot plus the extension, so `Foo.android.kt` moved to `Foo.nonIos.kt` counts and
`Foo.kt` to `Foo.swift` doesn't) and copied lines of a listed path that is gone. A file renamed with its copied code
needs `--allow-new`, like any new copy.

`--allow-new` is for a copy on purpose, the last section's coincidental similarity, or an `actual` per platform that
happens to match: say why in the pull request. The `duplication` workflow, a required check of `main`'s ruleset, runs
the same check on every pull request and every push to `main`, and shows each file whose copied lines grew against the
base branch (`main`, for most pull requests) in the job summary, behind one warning, so the reviewer sees what the
baseline took in.
