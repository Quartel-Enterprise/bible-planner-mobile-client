## Navigation

The app uses Navigation 3 (`org.jetbrains.androidx.navigation3:navigation3-ui`). The back stack is a
`NavBackStack<NavKey>` owned by `RootAppNavDisplay` and driven by `BackStackController`; there is no
`NavController`.

### 1. Define a type-safe route in `core/model/src/.../route/`

Every route implements `NavKey`:

```kotlin
// No arguments
@Serializable
data object SomeFeatureNavRoute : NavKey

// With arguments
@Serializable
data class SomeFeatureNavRoute(
    val id: Int,
    val type: String,
) : NavKey
```

Register the route in the polymorphic serializers module in
`core/model/src/.../route/Nav3SavedStateConfiguration.kt` (required for state restoration on
non-JVM targets):

```kotlin
subclass(SomeFeatureNavRoute::class, SomeFeatureNavRoute.serializer())
```

### 2. Navigate through the `Navigator`

Navigation is a single injectable dependency, not a `NavController` and not lambdas threaded down
from the root. Inject `Navigator` (`core/model`) into the ViewModel and call it:

- `navigate(route: NavKey)` — push a route
- `navigateBack()` — pop the top entry
- `navigateReplacingTop(route: NavKey)` — pop the current entry and push a route (the old
  `popUpTo(current) { inclusive = true }` pattern)

```kotlin
internal class SomeFeatureViewModel(
    route: SomeFeatureNavRoute,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<SomeFeatureUiEvent>(trackEvent) {
    override fun handleEvent(event: SomeFeatureUiEvent) = when (event) {
        SomeFeatureUiEvent.OnBackClick -> navigator.navigateBack()
        SomeFeatureUiEvent.OnDetailsClick -> navigator.navigate(SomeDetailsNavRoute)
    }
}
```

Each call sends a `NavigationCommand` over a buffered channel; `RootAppNavDisplay` collects the
commands and applies them to the back stack, so the back stack is only ever mutated from inside the
composition.

**Navigation never belongs in a `UiAction`.** `UiAction` is for effects the UI layer has to perform
itself — snackbars, clipboard, share sheets, scrolling, focus. The exceptions are effects that are
not root navigation at all: switching a bottom tab (`feature/main`) and a child screen calling back
into the parent that hosts it.

When a back callback is pure UI and never passes through a `UiEvent` — a dialog's close button, for
example — resolve the `Navigator` in the entry with `koinInject<Navigator>()` and pass
`navigator::navigateBack` down. Do not reach into DI from leaf composables; they keep their lambda
parameters.

### 3. Create an `EntryProviderScope` extension in `presentation/`

```kotlin
// SomeFeatureRoot.kt
fun EntryProviderScope<NavKey>.someFeature() {
    entry<SomeFeatureNavRoute> { route ->
        val viewModel = koinViewModel<SomeFeatureViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        SomeFeatureContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
```

Route arguments reach the ViewModel as a constructor parameter (`route: SomeFeatureNavRoute`)
injected via `parametersOf(route)` — never via `SavedStateHandle.toRoute()`.

For dialog destinations, mark the entry with `DialogSceneStrategy` metadata:

```kotlin
entry<SomeDialogNavRoute>(
    metadata = DialogSceneStrategy.dialog(
        DialogProperties(dismissOnClickOutside = false),
    ),
) { ... }
```

For shared-element transitions, the entry's animation scope is `LocalNavAnimatedContentScope.current`.

### 4. Register in `RootEntryProvider`

```kotlin
// core/navigation/src/.../RootEntryProvider.kt
internal fun SharedTransitionScope.toEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    mainScreen(sharedTransitionScope)
    day(sharedTransitionScope)
    someFeature()
}
```

Bottom-tab navigation lives in `feature/main`: `BottomNavTabState` keeps one `NavBackStack` per tab
and merges the decorated entries of the active tabs into a nested `NavDisplay`.

## Composable Structure

```kotlin
// presentation/content/SomeFeatureContent.kt
@Composable
fun SomeFeatureContent(
    uiState: SomeFeatureUiState,
    onEvent: (SomeFeatureUiEvent) -> Unit,
) {
    when (uiState) {
        SomeFeatureUiState.Loading -> SomeFeatureLoading()
        is SomeFeatureUiState.Loaded -> SomeFeatureLoaded(uiState, onEvent)
    }
}

@Composable
private fun SomeFeatureLoaded(
    uiState: SomeFeatureUiState.Loaded,
    onEvent: (SomeFeatureUiEvent) -> Unit,
) {
    // UI implementation
}
```

Sub-composables go in `presentation/component/`.
