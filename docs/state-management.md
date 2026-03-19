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

### UiEvent — user interactions sent to the ViewModel

```kotlin
sealed interface DayUiEvent {
    data class OnChapterClicked(val chapterNumber: Int) : DayUiEvent
    data object OnBackClicked : DayUiEvent
}
```

### UiAction — one-shot side effects (navigation, scroll)

```kotlin
sealed interface DayUiAction {
    data class NavigateTo(val route: SomeNavRoute) : DayUiAction
    data object ScrollToTop : DayUiAction
}
```

### ViewModel structure

```kotlin
class DayViewModel(
    private val useCases: DayUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<DayNavRoute>()

    private val _uiState = MutableStateFlow<DayUiState>(DayUiState.Loading)
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    private val _uiAction = MutableSharedFlow<DayUiAction>()
    val uiAction: SharedFlow<DayUiAction> = _uiAction.asSharedFlow()

    init {
        observeSomething()
    }

    fun onEvent(event: DayUiEvent) = when (event) {
        is DayUiEvent.OnChapterClicked -> handleChapterClicked(event.chapterNumber)
        DayUiEvent.OnBackClicked -> emitAction(DayUiAction.NavigateBack)
    }

    private fun observeSomething() {
        useCases.getSomethingFlow()
            .onEach { data -> _uiState.value = DayUiState.Loaded(data) }
            .launchIn(viewModelScope) // use observe() extension from ui/utils
    }

    private fun emitAction(action: DayUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }
}
```

Use the `observe()` extension from `ui/utils` instead of `.onEach { }.launchIn(viewModelScope)` when available.
