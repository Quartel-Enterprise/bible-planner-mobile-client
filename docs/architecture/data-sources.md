## Data Sources

### Room (reactive local database)

DAOs expose `Flow<T>` for reactive queries. Repositories map with `.map(mapper::map)`.

### Bundled JSON (ComposeResources)

Place files in `src/commonMain/composeResources/files/`. Read asynchronously on `Dispatchers.IO` using `async`/`awaitAll`, deserialize with `kotlinx.serialization`. See `PlanLocalDataSource` for reference.

### DataStore Preferences

Expose `Flow<T>` from `dataStore.data.map { preferences -> ... }`. See `ReadingPlanRepositoryImpl` for reference.
