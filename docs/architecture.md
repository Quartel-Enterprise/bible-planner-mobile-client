# Bible Planner — Architecture Guide for AI Assistants

This document describes the architecture conventions of this project. Follow these patterns exactly when creating new features or modifying existing ones.

---

## Stack

- **Kotlin Multiplatform (KMP)** targeting Android, iOS, and JVM Desktop
- **Compose Multiplatform** for UI
- **Koin** for dependency injection
- **Room** (KMP) for local database
- **DataStore Preferences** for user preferences
- **kotlinx.serialization** for JSON
- **kotlinx.coroutines** for async
- **Compose Navigation Multiplatform** with type-safe routes

---

## Module Structure

The project uses a multi-module Gradle setup. Every new feature gets its own module.

```
composeApp/          # App entry point
core/
├── books/           # Books domain + data + DI
├── model/           # Shared data models and nav routes
├── navigation/      # AppNavHost
├── plan/            # Plan data (JSON) domain + data
├── utils/           # Kotlin extension utilities
└── provider/
    ├── koin/        # Koin initialization — register new modules here
    ├── platform/    # Platform detection (expect/actual)
    ├── room/        # Room database, DAOs, entities
    └── data_store/  # DataStore setup
feature/
├── reading_plan/
├── day/
├── theme_selection/
├── material_you/
└── delete_progress/
ui/
├── theme/           # AppTheme, colors, Theme enum
├── component/       # Shared composables
└── utils/           # ActionCollector, observe() extension
```

### Within every `feature/` or `core/` module

```
src/commonMain/kotlin/.../
├── data/
│   ├── datasource/      # Local or remote data access
│   ├── dto/             # Raw data transfer objects (JSON parsing)
│   ├── mapper/          # DTO/Entity → Domain model mappers
│   └── repository/      # Repository implementations
├── di/                  # Koin module definition (one file per module)
├── domain/
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Use case interfaces + impl/ subpackage
└── presentation/
    ├── viewmodel/       # ViewModel
    ├── model/           # UiState, UiEvent, UiAction
    ├── mapper/          # Domain model → presentation model (if needed)
    ├── content/         # Content composables (Loaded/Loading variants)
    └── component/       # Sub-composables for the screen
```

---

## Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| Use case interface | `VerbNoun` or `VerbNounUseCase` | `GetSelectedReadingPlanFlow` |
| Use case impl | Same name + suffix in `impl/` | `GetSelectedReadingPlanFlowUseCase` |
| Repository interface | `NounRepository` | `BooksRepository` |
| Repository impl | `NounRepositoryImpl` | `BooksRepositoryImpl` |
| ViewModel | `FeatureViewModel` | `DayViewModel` |
| UiState | `FeatureUiState` | `DayUiState` |
| UiEvent | `FeatureUiEvent` | `DayUiEvent` |
| UiAction | `FeatureUiAction` | `DayUiAction` |
| Koin module | `featureModule` (val) | `readingPlanModule` |
| Nav route | `FeatureNavRoute` | `DayNavRoute` |
| DTO | `NounDto` | `WeekPlanDto` |
| Entity | `NounEntity` | `BookEntity` |
| Mapper | `NounMapper` / `NounMapperImpl` | `ReadingPlanPreferenceMapper` |
| Root composable extension | `NavGraphBuilder.featureName()` | `NavGraphBuilder.day()` |

---

## State Management (UDF)

Every feature has three sealed interfaces in `presentation/model/`:

### UiState — what the screen shows

O `UiState` não precisa ser necessariamente um `sealed interface`. Use o tipo mais simples que atenda à necessidade:

- **`sealed interface`** — quando a tela tem estados distintos (ex: Loading/Loaded/Error)
- **`data class`** — quando o estado é sempre "carregado" e apenas os dados variam
- **tipo primitivo** (`Boolean`, `String`, etc.) — quando o estado é simples o suficiente

```kotlin
// Quando há estados distintos (Loading/Loaded)
sealed interface DayUiState {
    data object Loading : DayUiState
    data class Loaded(
        val field1: Type,
        val field2: Type,
    ) : DayUiState
}

// Quando o estado é sempre presente
data class DayUiState(
    val field1: Type,
    val field2: Type,
)

// Quando o estado é um valor simples
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

---

## Use Cases

### Pattern A — `fun interface` (preferred for injectable abstractions)

```kotlin
// domain/usecase/GetSomethingFlow.kt
fun interface GetSomethingFlow {
    operator fun invoke(): Flow<Something>
}

// domain/usecase/impl/GetSomethingFlowUseCase.kt
class GetSomethingFlowUseCase(
    private val repository: SomeRepository,
) : GetSomethingFlow {
    override fun invoke(): Flow<Something> = repository.getSomethingFlow()
}
```

Koin binding:
```kotlin
factoryOf(::GetSomethingFlowUseCase).bind<GetSomethingFlow>()
```

### Pattern B — concrete class (for complex use cases that don't need an abstraction)

```kotlin
class MarkChapterAsReadUseCase(
    private val repository: DayRepository,
) {
    suspend operator fun invoke(chapterNumber: Int) {
        repository.markChapterRead(chapterNumber)
    }
}
```

### Use case aggregation (when a ViewModel needs many use cases)

```kotlin
// domain/usecase/DayUseCases.kt
data class DayUseCases(
    val getSomethingFlow: GetSomethingFlow,
    val markChapterAsRead: MarkChapterAsReadUseCase,
    val anotherUseCase: AnotherUseCase,
)
```

Koin:
```kotlin
factoryOf(::DayUseCases)
viewModelOf(::DayViewModel)
```

---

## Repository Pattern

```kotlin
// domain/repository/SomeRepository.kt
interface SomeRepository {
    fun getSomethingFlow(): Flow<Something>
    suspend fun doAction(param: Type)
}

// data/repository/SomeRepositoryImpl.kt
class SomeRepositoryImpl(
    private val dao: SomeDao,
    private val mapper: SomeMapper,
) : SomeRepository {
    override fun getSomethingFlow(): Flow<Something> =
        dao.getAllFlow().map(mapper::map)

    override suspend fun doAction(param: Type) {
        dao.insert(mapper.toEntity(param))
    }
}
```

---

## Dependency Injection (Koin)

Every module has exactly one file in `di/`:

```kotlin
// di/someFeatureModule.kt
val someFeatureModule = module {
    singleOf(::SomeRepositoryImpl).bind<SomeRepository>()
    factoryOf(::GetSomethingFlowUseCase).bind<GetSomethingFlow>()
    factoryOf(::AnotherUseCaseImpl)
    factoryOf(::SomeUseCases)
    viewModelOf(::SomeViewModel)
}
```

**Register the new module** in `core/provider/koin/src/commonMain/.../CommonKoinUtils.kt`:

```kotlin
val modules = listOf(
    // ... existing modules
    someFeatureModule,
)
```

---

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

---

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

---

## Data Sources

### Room (reactive local database)

DAOs expose `Flow<T>` for reactive queries. Repositories map with `.map(mapper::map)`.

### Bundled JSON (ComposeResources)

Place files in `src/commonMain/composeResources/files/`. Read asynchronously on `Dispatchers.IO` using `async`/`awaitAll`, deserialize with `kotlinx.serialization`. See `PlanLocalDataSource` for reference.

### DataStore Preferences

Expose `Flow<T>` from `dataStore.data.map { preferences -> ... }`. See `ReadingPlanRepositoryImpl` for reference.

---

## Checklist for a New Feature

- [ ] Create a new Gradle module under `feature/` with the standard layer structure
- [ ] Define `UiState`, `UiEvent`, `UiAction` in `presentation/model/`
- [ ] Define repository interface in `domain/repository/`
- [ ] Implement repository in `data/repository/`
- [ ] Define use cases in `domain/usecase/` (use `fun interface` + `impl/` pattern)
- [ ] Group use cases in a `FeatureUseCases` data class if there are more than 2
- [ ] Implement `FeatureViewModel` with `StateFlow<UiState>` and `SharedFlow<UiAction>`
- [ ] Create `FeatureContent` composable with Loading/Loaded split
- [ ] Define a `@Serializable` route in `core/model/.../route/`
- [ ] Create `NavGraphBuilder.feature()` extension (Root composable)
- [ ] Register the route in `AppNavHost`
- [ ] Create a Koin module in `di/` and register it in `CommonKoinUtils`
- [ ] Add the new Gradle module to `settings.gradle.kts`

---

## Pull Request Guidelines

- PR titles and descriptions must be written in **English**
