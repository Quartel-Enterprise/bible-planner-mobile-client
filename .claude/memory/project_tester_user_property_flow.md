---
name: project_tester_user_property_flow
description: is_tester Analytics user property gates pre-prod feature flags; set by combining auth + tester_user_ids allowlist.
metadata: 
  node_type: memory
  type: project
  originSessionId: a9ea4a04-1da2-4425-bf79-ec48a79ff2fc
---

To enable feature flags only for testers before prod, the app sets a Firebase Analytics user property **`is_tester`** (`"true"`/`"false"`), which Remote Config conditions segment on. It is driven by `ObserveTesterUserPropertyUseCase` in `core/provider/analytics`, which `combine`s `ObserveAuthenticatedUserId` (Supabase user id) with `ObserveStringRemoteConfig("tester_user_ids")` (a **JSON-typed** Remote Config param — a JSON array of Supabase user IDs, e.g. `["uuid1","uuid2"]`, parsed with `Json.decodeFromString(ListSerializer(String.serializer()), ...)`) and calls `AnalyticsService.setUserProperty`. Launched from `InitializeAppContentUseCase`.

`AnalyticsService` is a new provider module (`core/provider/analytics`): Android wraps `FirebaseAnalytics`, iOS is Swift `IosAnalyticsService` injected via `initializeKoinForIos`, JVM is a no-op. Relies on the observe-first RemoteConfig — see [[project_remoteconfig_observe_first]].

Manual setup still required in the Firebase console: define `tester_user_ids`, register the `is_tester` Analytics custom definition, and add an `is_tester == true` condition on each flag (e.g. `is_premium_verification_required`).
