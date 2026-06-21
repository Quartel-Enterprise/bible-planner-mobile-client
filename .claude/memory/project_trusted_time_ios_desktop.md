---
name: project_trusted_time_ios_desktop
description: "iOS/desktop trusted-time strategy uses the HTTP Date header (ktor) anchored to a monotonic clock, vs Android's native TrustedTime API."
metadata: 
  node_type: memory
  type: project
  originSessionId: 2deb2805-35b7-4c61-96d5-dab90f943144
---

Part of epic #179 (trusted time across platforms). `CurrentTimestampProvider` (core/date) abstracts "now" for synced timestamps; fallback is `DeviceClockTimestampProvider` (device wall clock).

- **Android** (issue #185, merged; chain extended in #183): `AndroidTrustedTimestampProvider` uses Google Play Services `TrustedTime`, falling back to `NetworkTimeTimestampProvider` (HTTP Date), then device clock. Chain: TrustedTime → NetworkTime → DeviceClock (helps devices without Play Services).
- **iOS + desktop/JVM** (issue #183): `NetworkTimeTimestampProvider` in `commonMain`, bound in `DateModule.ios.kt` and `DateModule.jvm.kt`. On init it launches one async sync on `ApplicationScope`: `HEAD`s a reference URL, reads the `Date` response header (`fromHttpToGmtDate().timestamp`), and stores `(serverTimestamp, TimeSource.Monotonic.markNow())`. `getCurrentTimestamp()` = `serverTimestamp + mark.elapsedNow()`, else device clock.

**Why:** iOS has no native trusted-time API. NTP/SNTP via ktor-network UDP is unreliable on iOS native (Kotlin/Native), so we use the HTTP `Date` header over the existing ktor `HttpClient` (core/network) — no new deps, no cinterop/POSIX, ~1s precision (fine for read-progress timestamps). Anchoring to the monotonic clock makes it resist manual wall-clock changes.

**How to apply:** reference server is `BuildKonfig.SUPABASE_URL` — core/date has its own buildkonfig block reading `SUPABASE_URL` from `local.properties` (same secret source as the supabase module, single source of truth = local.properties; no module coupling). See [[feedback_use_suspendruncatching]].
