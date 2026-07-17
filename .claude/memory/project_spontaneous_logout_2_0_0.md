---
name: spontaneous-logout-2-0-0
description: "2.0.0 spontaneous-logout investigation — logout is CLIENT-LOCAL (server sessions stay alive/orphaned); revoke/prune edge functions are innocent; supabase-kt clears local session on any 4xx refresh, on signOut with 401/403/404 (without revoking server-side), and on session_not_found."
metadata: 
  node_type: memory
  type: project
  originSessionId: 39770d73-177d-416c-a01c-abc9659d581e
---

Investigated 2026-07-14: users (Pierre's SM-S948B 3x in 36h, plus real users SM-A135F / cristinadias) get logged out spontaneously after 2.0.0 (PR #255 connected-devices).

**Server exonerated**: auth.sessions rows and refresh tokens of "logged-out" sessions remain valid and UNUSED (orphaned); user_devices rows never deleted server-side; no /logout, no failed /token in auth logs. revoke-device-session + prune_revoked_user_devices never fired wrongly.

**Logout is client-local** (supabase-kt 3.6.0 AuthImpl):
- refresh failure with ANY 4xx → clearSession() (only 5xx/network retry)
- signOut() tolerates 401/403/404 and clears LOCAL session while server session survives → exact orphan signature observed
- any auth response with error code session_not_found → clearSession()
- sessionManager.loadSession() failure → starts logged out silently

**Suspect chain**: `ObserveCurrentDeviceRevoked().collect { endSession() }` (InitializeAppContentUseCase) is the only programmatic logout, new in 2.0.0, fires on LOCAL Room row disappearance, zero instrumentation. endSession with expired access token → signOut 401 → local-only logout. Exact local row-deletion trigger not yet proven (candidates: retainOnly race in DevicesSynchronizer.pullSnapshot vs registration, clearLocal on account-switch race in [[SyncCoordinator]]).

**Collateral bugs found**: (1) sync push loop keeps retrying as ANON after session loss → Crashlytics issue 504627f2 (42501 RLS verse_reads, firstSeen 2.0.0, ~60s maxBackoff loop in background); (2) edge auth-middleware gets publishable key (sb_..., len 46) as Bearer when clients have no session → all-day GET /user 403 "token malformed" noise in auth logs; (3) orphaned auth.sessions accumulate (some since January).

Next steps agreed direction: instrument first (pipe supabase-kt logger into kermit to capture "Clearing session (Status code X)"; track endSession-by-revoked), server-confirm before endSession, gate sync pushes on session presence.

**PR #263 (branch enhancement/instrument-session-loss)** implements the instrumentation:
- `session_lost` event + `SessionLostException` non-fatal via `ObserveSessionLossUseCase` (feature/logout); `IntentionalLogoutMarker` (core/user singleton) set by EndSession so app-initiated logouts are excluded. Params: `source`, `is_access_token_expired`.
- `current_device_revoked` event + `CurrentDeviceRevokedException` non-fatal via `HandleCurrentDeviceRevokedUseCase` (feature/logout), replacing the inline endSession in InitializeAppContentUseCase.
- **`server_session_state` param** (revoked/active/unknown) on current_device_revoked: `CheckRemoteSessionState` (core/user) probes via `auth.refreshCurrentSession()` — the refresh token is the session-bound credential (access-token JWT stays valid ~1h post-revocation, so only refresh reveals revocation). 4xx→revoked (legit remote sign-out), success→active (FALSE POSITIVE, the bug signal), else→unknown. Marker is set BEFORE the probe so the probe's rare async clearSession can't misfire session_lost. Still endSessions regardless (instrumentation-only).
- Forwarded supabase-kt logs to Crashlytics via `KermitSupabaseLoggingProcessor` (core/provider/supabase, `defaultLoggingFactory`), wrapping message-only errors in `SupabaseLogException`, so "Clearing session (Status code X)" surfaces as a non-fatal.

**#263 MERGED to main (2026-07-15).** Two follow-up PRs opened off main:
- **PR #264 (fix/gate-device-revoked-logout-on-server-truth)** — the false-positive-logout fix: `HandleCurrentDeviceRevokedUseCase` now ends the session ONLY when `server_session_state==revoked`; on `active`/`unknown` it keeps the user signed in and unmarks the intent. `active` → `FalsePositiveDeviceRevocationException` non-fatal (renamed from CurrentDeviceRevokedException). Follow-up idea: re-register on active to restore the device's own row (missing from its list until next pull/restart).
- **PR #265 MERGED to main (2026-07-15), targeted for release 2.0.1 (added 2.0.1 release notes, pt/en/es, framed as background battery/data fix — NOT as a logout fix, since #264 isn't merged)** — the collateral 42501: `OfflineFirstSynchronizer.runPushLoop` (and `DevicesSynchronizer`) captured userId once and retried on ANY failure, so after session loss the push retried forever as anon (Bearer sb_publishable_, len 46) → RLS 42501 every ~60s. Fix: re-read `getAuthenticatedUserId()` at the top of each retry iteration, bail if null (turns infinite anon-retry into ≤1 stray attempt).

**2.0.1 (build 33) production data (2026-07-15, repro confirmed on pierrevieiraggg's SM-S948B):** Elimination narrowed it hard.
- `SessionLostException` (session_lost) = **0**, "Clearing session (Status code X)" = **0**, session_not_found = **0** → the ENTIRE supabase-kt in-app clear family (background refresh 4xx / signOut / session_not_found) is RULED OUT. It is NOT a token/refresh-rejection problem.
- `current_device_revoked` = **1 total**, from a Xiaomi (NOT pierrevieiraggg), `server_session_state=unknown` (network down, correlated SocketException), fired in BACKGROUND ~1min after sync_completed. Validates #264: `unknown`→keep-signed-in would have prevented it. Low volume, not the dominant driver.
- pierrevieiraggg's SM-S948B: produced NO session_lost, NO current_device_revoked, NO clear-log. Only `SettingsSessionManager.loadSession "No entry with the key sb-...-session"` at startup (03:26, both AuthImpl.init cold-start AND SetupPlatform onStart foreground-reload paths). DB: sessions orphaned/alive as before, new ones at 03:26/14:01/16:49, device row present+fresh.
- Mechanism conclusion by elimination: supabase-kt's `deleteSession()` is only called by `clearSession()`, whose callers all leave a trace (log or current_device_revoked) — NONE present. App has no other signOut/endSession/Logout caller (grep clean), no delete-account, no android:process multi-process, default SettingsSessionManager (SharedPreferences). So the session vanishes from SharedPreferences between runs **without any code deleting it** → the dominant root cause is at the session PERSISTENCE layer (SharedPreferences durability / external wipe / allowBackup), NOT the auth logic.
- **Instrumentation flaw found:** the KermitSupabaseLoggingProcessor promotes supabase-kt's benign "No session at startup" ERROR (loadSession throws by design when key absent) into a Crashlytics non-fatal → 35 events of NOISE (fires on every logged-out foreground/cold-start). Needs a filter (drop that specific loadSession/No-entry ERROR, or don't forward loadFromStorage failures).
- Next step to close the dominant case: wrap the Supabase SessionManager with a decorator that logs/tracks every saveSession + deleteSession (stack trace on delete) to catch the culprit red-handed; check Android allowBackup and multiplatform-settings persistence.

**PR #269 (enhancement/instrument-session-storage)** implements the storage audit: `MonitoredSessionManager` decorator (core/provider/supabase, package `core.provider.supabase.session`) wraps platform `SettingsSessionManager` (expect/actual `createPlatformSessionManager`, injected via Auth config `sessionManager`); audit trail `DataStoreSessionAuditStore` records lastSavedAt/lastDeletedAt in DataStore (separate file from the SharedPreferences holding the session, so a wipe of one keeps evidence in the other). Signals: `SessionDeletedException` non-fatal on every delete (stack = caller); `SessionStorageLossException` when loadSession finds no entry but audit says save-never-deleted (fires ONCE per loss, audit reset). Also fixed the #263 instrumentation flaw: KermitSupabaseLoggingProcessor now downgrades the benign `IllegalStateException "No entry with the key"` ERROR (thrown by design on every logged-out cold start AND foreground via SetupPlatform.onStart) to debug — it was 35 junk non-fatals/day (issue f31d6b20).

**#264 and #269 MERGED to main (2026-07-15), targeted for release 2.0.2** (2.0.1/build 33 was already cut and released — has "chore: merge back 2.0.1 into main (#268)" — by the time #264/#269 were ready; both branches were merged-updated from main, not rebased, before landing). Added 2.0.2 release notes (pt/en/es): "Fixed an issue that could sign you out of your account on its own in some cases" — worded to cover the confirmed false-positive fix (#264) without over-promising on Pierre's still-open storage-loss case.

State after this release: #264's gate is live (prevents the Xiaomi-style false positive from logging anyone out). #269's SessionStorageLossException/SessionDeletedException are live and will fire on the NEXT spontaneous-logout repro — that's the next data checkpoint to actually close Pierre's case.

**Backend exonerated for the dominant (Pierre) case** — evidence: server never rejected/revoked anything (sessions alive+orphaned, tokens valid unused, no /logout, no failed /token, zero client "Clearing session" logs); a backend cannot remove data from device SharedPreferences. BE code audit (edge fns register-device/revoke-device-session/auth-middleware + migrations prune/is_session_active/revoke_user_device_session) found them scoped correctly and DB shows they never fired against Pierre's rows. Only remaining BE-adjacent suspect: prune/realtime-DELETE wrongly removing a user_devices ROW feeds the current_device_revoked path (Xiaomi case) — #264 gates it, and server_session_state=active occurrences will localize it.

Still open after these: fix whatever DELETES the local device row in the first place (the active non-fatal + current_device_revoked analytics will localize it — candidates: retainOnly race in pullSnapshot vs registration, spurious realtime DELETE, clearLocal account-switch race); orphaned auth.sessions accumulation.

Note: while working, there was an unrelated uncommitted WIP change in `feature/day/.../LoadedDayLandscapeScreenContent.kt` in the working tree (not mine) — left untouched, excluded from all commits.
