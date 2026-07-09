## Navigation

The app uses Navigation 3 (`org.jetbrains.androidx.navigation3:navigation3-ui`). The back stack is a
`NavBackStack<NavKey>` owned by `RootAppNavDisplay` and mutated directly; there is no `NavController`.

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

### 2. Create an `EntryProviderScope` extension in `presentation/`

Navigation is expressed through lambdas instead of a `NavController`. Take only the ones the
feature needs:

- `onNavigate: (Any) -> Unit` — push a route
- `onNavigateBack: () -> Unit` — pop the top entry
- `onNavigateReplacingTop: (Any) -> Unit` — pop the current entry and push a route (the old
  `popUpTo(current) { inclusive = true }` pattern)

```kotlin
// SomeFeatureRoot.kt
fun EntryProviderScope<NavKey>.someFeature(
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<SomeFeatureNavRoute> { route ->
        val viewModel = koinViewModel<SomeFeatureViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ActionCollector(viewModel.uiAction) { action ->
            when (action) {
                SomeFeatureUiAction.NavigateBack -> onNavigateBack()
                is SomeFeatureUiAction.NavigateTo -> onNavigate(action.route)
            }
        }

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

### 3. Register in `RootAppNavDisplay`

```kotlin
// core/navigation/src/.../RootAppNavDisplay.kt
entryProvider = entryProvider {
    mainScreen(...)
    day(...)
    someFeature(
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
    )
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
