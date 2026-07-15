# session_lost

**Tier:** P1 | **Domain:** Auth

Captures a session that ended locally without the app asking for it: the Supabase session status went from `Authenticated` to `NotAuthenticated` and no app-initiated logout was in flight. This is the "user got logged out by itself" rate — the spontaneous-logout investigation of 2026-07-14 showed the server never revoked those sessions (they stay alive and orphaned in `auth.sessions`), so only client-side telemetry can reveal how often this happens and in which state.

## When it fires

`ObserveSessionLossUseCase` sees a transition from an `Authenticated` status (possibly with a `RefreshFailure` in between) to `NotAuthenticated`, and `IntentionalLogoutMarker` was not marked by `EndSessionUseCase`. Known supabase-kt paths that clear the session locally: a refresh rejected with any 4xx, a `signOut()` answered with 401/403/404 (local clear without server revocation), and a `session_not_found` error code on any auth response.

## Trigger source

`feature/logout/.../domain/usecase/ObserveSessionLossUseCase.kt` — launched from `InitializeAppContentUseCase` (not a ViewModel; background pipeline)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `sign_in` \| `refresh` \| `storage` \| `external` \| `sign_up` \| `anonymous_sign_in` \| `user_changed` \| `user_identities_changed` \| `unknown` | Where the lost session had come from (`SessionSource` of the last `Authenticated` status) |
| `is_access_token_expired` | boolean | `true` | Whether the access token was already expired when the session was lost — expired points at the refresh path, non-expired at a mid-life clear (e.g. a failed `signOut`) |

## Notes

- Also reported to Crashlytics as a non-fatal (`SessionLostException`) so it can be correlated on the device timeline with the forwarded supabase-kt logs ("Clearing session (Status code X)").
- An intentional logout whose `signOut` fails un-marks the intent, so a session cleared right after a failed logout may still count here; that corner is rare and worth seeing.
- Cold starts that already begin logged out are not counted — there is no previous `Authenticated` status to compare against, so a session lost while the app was dead is invisible to this event.
