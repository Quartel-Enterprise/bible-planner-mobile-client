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
