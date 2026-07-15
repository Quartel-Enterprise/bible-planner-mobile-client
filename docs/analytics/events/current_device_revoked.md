# current_device_revoked

**Tier:** P1 | **Domain:** Auth

Captures this device detecting that its own row disappeared from the synced connected-devices list, which makes the app end the local session (the remote sign-out flow introduced in #255). The detection is based on local Room state, which can't by itself tell a legitimate remote sign-out from a false positive — so before ending the session the app probes the server (`server_session_state`) to classify each occurrence. Compare its rate with `device_signed_out` (the sender side) as a cross-check.

## When it fires

`ObserveCurrentDeviceRevoked` emits (the current device's row disappeared from the local store after having been present); `HandleCurrentDeviceRevokedUseCase` then probes the server and fires this event right before `EndSession` runs.

## Trigger source

`feature/logout/.../domain/usecase/HandleCurrentDeviceRevokedUseCase.kt` — driven by `InitializeAppContentUseCase` (not a ViewModel; background pipeline)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `server_session_state` | string | `revoked` \| `active` \| `unknown` | Result of the server probe (a token refresh — the refresh token is the session-bound credential, unlike the ~1h-valid access token). `revoked`: the session is really gone server-side → a legitimate remote sign-out. `active`: the session still lives → a **false positive**, the local row vanished for another reason (this is the bug signal). `unknown`: the probe was inconclusive (offline or a 5xx). |

## Notes

- `active` is the value to watch: a sustained rate of it means false-positive revocations are logging users out. `revoked` is the healthy path.
- Also reported to Crashlytics as a non-fatal (`CurrentDeviceRevokedException`, carrying the same state) for device-timeline correlation with `session_lost` and the forwarded supabase-kt logs.
- The path marks the logout as intentional before the probe, so it never double-counts as `session_lost` — even in the `active` case, where the probe's success would otherwise leave the session momentarily alive.
- Today the session is ended regardless of `server_session_state`; gating the sign-out on a `revoked` result (the actual fix for the false-positive logout) is deferred to a follow-up once this data confirms the rate.
