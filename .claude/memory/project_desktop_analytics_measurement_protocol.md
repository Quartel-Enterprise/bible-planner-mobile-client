---
name: project_desktop_analytics_measurement_protocol
description: "Desktop (JVM) analytics via GA4 Measurement Protocol REST; issue #196; desktop crashes deferred to Sentry #237"
metadata: 
  node_type: memory
  type: project
  originSessionId: 7a860fb2-da3a-425f-b0f3-f4611cc9ad8a
---

Issue #196 (sub-issue of desktop epic #195): desktop `AnalyticsService` was a no-op; now a real JVM impl posts to the **GA4 Measurement Protocol** (REST) reusing Ktor. Lives entirely in `core/provider/analytics/src/jvmMain` (no commonMain change, `AnalyticsService` interface unchanged — still only `setUserProperty`).

**How it works:** `DesktopAnalyticsService.setUserProperty` keeps an in-memory user-properties map and fire-and-forgets (`CoroutineScope(Dispatchers.IO + SupervisorJob())`) a `session_start` event via `MeasurementProtocolClient` carrying the current user properties. `ObserveTesterUserPropertyUseCase` (runs on desktop via `InitializeAppContent` → `AppViewModel`) drives it, so `is_tester` registers **and** a visible session event is produced per launch (bounded by upstream `distinctUntilChanged`). `MeasurementProtocolClient` POSTs to `https://www.google-analytics.com/mp/collect`, builds the body with `buildJsonObject{}` (no @Serializable DTO), always tags `user_properties.platform = "desktop"`.

**Non-obvious decisions:**
- **Transport = a dedicated GA4 *Web* data stream** (`measurement_id` G-XXXX + `client_id`), NOT the Android `firebase_app_id`, chosen so desktop is **isolatable from iOS/Android** in Firebase→Analytics (own stream, platform "web", plus the explicit `platform=desktop` prop). The issue text said "firebase_app_id" but we went Web-stream for data hygiene.
- **Provisioned (2026-07-03):** dedicated Firebase Web app "Bible Planner Desktop" (app id `1:169733566422:web:fca2faeea1cd99134a7e77`) → GA4 Web data stream `G-9T13HGX62H` (stream id `15195730302`, account `377953152` / property `516768577`) with an MP API secret nicknamed "Desktop app". Both keys are in local `local.properties` (git-ignored); still TODO to add them to the `LOCAL_PROPERTIES` GitHub secret once desktop is built in CI. Shipped in PR #238.
- **Delivery confirmed** via a `mp_delivery_probe` sent to both the old web stream and the new desktop stream (Realtime showed count 2). **Gotcha:** a freshly-created GA4 data stream has a **warm-up lag (~45+ min)** before Realtime populates — MP returns 204 and `debug/mp/collect` returns clean even for bogus creds, so neither proves delivery; only Realtime/Reports on the stream confirms. Don't panic if a brand-new stream shows nothing for a while.
- Secrets `GA_MEASUREMENT_ID` + `GA_MEASUREMENT_API_SECRET` via **BuildKonfig** from `local.properties` (object `AnalyticsBuildKonfig`), same pattern as SUPABASE_*/GH_TOKEN. **Collection gate = if either secret blank → `send()` no-ops** (no separate desktop isDebug), so dev without secrets stays silent. User must provision the Web stream + api_secret and add both keys to `local.properties` and the `LOCAL_PROPERTIES` CI secret. Note: `desktopApp` is NOT built in release.yml yet (Android/iOS only).
- `client_id` = stable per-install UUID via JDK **`java.util.prefs.Preferences`** (node `com/quare/bibleplanner/analytics`), zero new deps (DataStore's preferences dep is `implementation`, not transitive).

**Desktop crashes deferred** to new sub-issue **#237** (under #195): Firebase Crashlytics has no JVM SDK and no write/ingestion REST API (its API is read-only) → desktop crashes can never reach the Crashlytics console. #237 proposes **Sentry** (JVM SDK) for a real crash console; interim option is `app_exception` GA4 events through this same MP pipeline. `DesktopCrashReporter` stays a no-op until then.

Related: [[project_crashlytics_integration]], [[project_tester_user_property_flow]], [[project_remoteconfig_observe_first]].
