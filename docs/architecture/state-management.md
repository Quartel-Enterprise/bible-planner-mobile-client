## State Management (UDF)

Every feature has three types in `presentation/model/`: `UiState`, `UiEvent`, and `UiAction`.

### UiState — what the screen shows

`UiState` does not always need to be a `sealed interface`. Use the simplest type that fits the need:

- **`sealed interface`** — when the screen has distinct states (e.g. Loading/Loaded/Error)
- **`data class`** — when the state is always "loaded" and only the data varies
- **primitive type** (`Boolean`, `String`, etc.) — when the state is simple enough

```kotlin
// When there are distinct states (Loading/Loaded)
sealed interface DayUiState {
    data object Loading : DayUiState
    data class Loaded(
        val field1: Type,
        val field2: Type,
    ) : DayUiState
}

// When the state is always present
data class DayUiState(
    val field1: Type,
    val field2: Type,
)

// When the state is a simple value
typealias DayUiState = Boolean
```

**UiState fields never have default values.** Every property must be explicitly set at construction. This forces the ViewModel to declare its initial state in full and prevents a new field from silently defaulting (and hiding a missing initial-value decision) when added later.

```kotlin
// Wrong — defaults hide what the initial state actually is
data class LoginUiState(
    val enabledProviders: List<LoginProvider>,
    val isGoogleLoading: Boolean = false,
)

// Correct — caller must pass every field
data class LoginUiState(
    val enabledProviders: List<LoginProvider>,
    val isGoogleLoading: Boolean,
)
```

### UiEvent — user interactions sent to the ViewModel

```kotlin
sealed interface DayUiEvent {
    data class OnChapterClicked(val chapterNumber: Int) : DayUiEvent
    data object OnBackClicked : DayUiEvent
}
```

### UiAction — one-shot side effects the UI layer performs

```kotlin
sealed interface DayUiAction {
    data class ShowSnackBar(val message: StringResource) : DayUiAction
    data object ScrollToTop : DayUiAction
}
```

Navigation is **not** a `UiAction` — inject `Navigator` and call it from the ViewModel. See
[navigation.md](navigation.md).

### ViewModel structure

```kotlin
class DayViewModel(
    private val useCases: DayUseCases,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<DayNavRoute>()

    val uiState: StateFlow<DayUiState>
        field = MutableStateFlow<DayUiState>(DayUiState.Loading)

    val uiAction: SharedFlow<DayUiAction>
        field = MutableSharedFlow<DayUiAction>()

    init {
        observeSomething()
    }

    fun onEvent(event: DayUiEvent) = when (event) {
        is DayUiEvent.OnChapterClicked -> handleChapterClicked(event.chapterNumber)
        DayUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun observeSomething() {
        useCases.getSomethingFlow()
            .onEach { data -> uiState.value = DayUiState.Loaded(data) }
            .launchIn(viewModelScope) // use observe() extension from ui/utils
    }

    private fun emitAction(action: DayUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }
}
```

Expose mutable state through an [explicit backing field](https://kotlinlang.org/docs/properties.html#explicit-backing-fields)
instead of a `_uiState` backing property plus `asStateFlow()`: the public type stays read-only while the class itself
smart-casts to the mutable one. Keep the type argument on the constructor (`MutableStateFlow<DayUiState>(...)`) — without
it the field is inferred from the initial value (`MutableStateFlow<DayUiState.Loading>`) and later assignments stop
compiling. The `bible-planner-style:explicit-backing-field` ktlint rule flags the old pattern. A `Channel` exposed through
`receiveAsFlow()` is not a subtype of its public type, so it keeps a private backing property.

Use the `observe()` extension from `ui/utils` instead of `.onEach { }.launchIn(viewModelScope)` when available.
