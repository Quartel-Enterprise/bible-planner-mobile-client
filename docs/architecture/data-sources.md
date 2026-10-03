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

### Offline-first sync (Supabase)

Room is the source of truth the UI observes; `OfflineFirstSynchronizer` (`core/sync`) reconciles it with Supabase,
**Last-Write-Wins** by timestamp. Every synced row carries an `updatedAt`-style timestamp and a pending flag.

- **Push** — `runPushLoop` upserts pending rows while the OS reports connectivity. The pending flag is cleared only
  if the row was not touched again while the push was in flight (`SyncLocalStore.markSynced`).
- **Pull** — `fetchSnapshot` fetches the full remote set. `SyncCoordinator` runs it on every realtime `CONNECTED`
  transition, cold start included, which covers offline gaps and triggers `adoptProvisionalDefaults`.
- **Realtime** — `observeRealtime` applies live inserts and updates.
- Every remote write goes through `applyRemote`, which only overwrites rows that are strictly older **and** not
  pending, so a local change made offline is never lost to an older server value.

Scalar settings share one generic table (`SyncedPreferenceDao`): a new synced setting is just a new key, reusing the
same pending flag and Last-Write-Wins mechanics, with no sync plumbing of its own.
