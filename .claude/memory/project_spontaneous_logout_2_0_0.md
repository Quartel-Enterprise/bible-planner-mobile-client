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

Deferred to a follow-up (not in #263): gate the endSession on server_session_state==revoked (the actual false-positive-logout fix); gate sync push loop on session presence to stop the anon 42501 retry loop.
