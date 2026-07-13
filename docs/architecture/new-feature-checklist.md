## Checklist for a New Feature

- [ ] Create a new Gradle module under `feature/` with the standard layer structure
- [ ] Define `UiState`, `UiEvent`, `UiAction` in `presentation/model/` (each `UiEvent` implements `UiEvent` marker and declares its `analytics` decision)
- [ ] Define repository interface in `domain/repository/`
- [ ] Implement repository in `data/repository/`
- [ ] Define use cases in `domain/usecase/` (use `fun interface` + `impl/` pattern)
- [ ] Group use cases in a `FeatureUseCases` data class if there are more than 2
- [ ] Implement `FeatureViewModel` extending `TrackedViewModel<FeatureUiEvent>` (implement `handleEvent`), with `StateFlow<UiState>` and `SharedFlow<UiAction>`
- [ ] Create `FeatureContent` composable with Loading/Loaded split
- [ ] Define a `@Serializable` route implementing `NavKey` in `core/model/.../route/`
- [ ] Register the route in `Nav3SavedStateConfiguration` (polymorphic serializers module)
- [ ] Create `EntryProviderScope<NavKey>.feature()` extension (Root composable)
- [ ] Register the entry in `RootAppNavDisplay`
- [ ] Create a Koin module in `di/` and register it in `CommonKoinUtils`
- [ ] Add the new Gradle module to `settings.gradle.kts`
- [ ] Catalog analytics events (run the `add-analytics-event` skill): declare each `UiEvent`'s `analytics`, add `AnalyticsEventNames`/`AnalyticsParams` constants, and add `docs/analytics/events/<name>.md` — see [docs/analytics/README.md](../analytics/README.md)
