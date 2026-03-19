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
