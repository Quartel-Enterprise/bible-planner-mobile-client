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
