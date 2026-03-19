## Navigation

### 1. Define a type-safe route in `core/model/src/.../route/`

```kotlin
// No arguments
@Serializable
data object SomeFeatureNavRoute

// With arguments
@Serializable
data class SomeFeatureNavRoute(
    val id: Int,
    val type: String,
)
```

### 2. Create a `NavGraphBuilder` extension in `presentation/`

```kotlin
// SomeFeatureRoot.kt
fun NavGraphBuilder.someFeature(navController: NavController) {
    composable<SomeFeatureNavRoute> {
        val viewModel = koinViewModel<SomeFeatureViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ActionCollector(viewModel.uiAction) { action ->
            when (action) {
                SomeFeatureUiAction.NavigateBack -> navController.popBackStack()
                is SomeFeatureUiAction.NavigateTo -> navController.navigate(action.route)
            }
        }

        SomeFeatureContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
```

### 3. Register in `AppNavHost`

```kotlin
// core/navigation/src/.../AppNavHost.kt
fun AppNavHost(...) {
    NavHost(...) {
        readingPlan(navController)
        day(navController)
        someFeature(navController) // add here
    }
}
```

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
