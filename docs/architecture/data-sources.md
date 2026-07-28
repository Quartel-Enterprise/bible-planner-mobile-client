## Data Sources

### Room (reactive local database)

DAOs expose `Flow<T>` for reactive queries. Repositories map with `.map(mapper::map)`.

### Bundled JSON (ComposeResources)

Place files in `src/commonMain/composeResources/files/`. Read asynchronously on `Dispatchers.IO` using `async`/`awaitAll`, deserialize with `kotlinx.serialization`. See `PlanLocalDataSource` for reference.

### DataStore Preferences

Expose `Flow<T>` from `dataStore.data.map { preferences -> ... }`. See `ReadingPlanRepositoryImpl` for reference.

### Firebase Remote Config

Feature flags are read through `core/remote_config`: use cases depend on `RemoteConfigService` (Flow-based), which sits on a per-platform `RemoteConfigDataSource`. Android and iOS use the Firebase SDK. The JVM/desktop target has no Firebase SDK and Remote Config has no public client REST API, so `DesktopRemoteConfigService` fetches the same template from the `get-remote-config` Supabase edge function (in the `bible-planner-api` repo), which reads it server-side with the service account. The Firebase Console stays the single source of truth for every platform.

Desktop specifics: values refresh every 15 minutes (there is no realtime push), only **default** values resolve — conditional values are not evaluated server-side — and `tester_user_ids` comes back filtered to the caller.
