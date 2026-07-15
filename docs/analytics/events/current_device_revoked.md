# current_device_revoked

**Tier:** P1 | **Domain:** Auth

Captures this device detecting that its own row disappeared from the synced connected-devices list — the remote sign-out flow introduced in #255. The detection is based on local Room state, which can't by itself tell a legitimate remote sign-out from a false positive, so the app probes the server (`server_session_state`) and **only ends the session when the server confirms the revocation** (`revoked`); on `active`/`unknown` it keeps the user signed in. Compare its rate with `device_signed_out` (the sender side) as a cross-check.

## When it fires

`ObserveCurrentDeviceRevoked` emits (the current device's row disappeared from the local store after having been present); `HandleCurrentDeviceRevokedUseCase` probes the server and fires this event, then ends the session only when `server_session_state` is `revoked`.

## Trigger source

`feature/logout/.../domain/usecase/HandleCurrentDeviceRevokedUseCase.kt` — driven by `InitializeAppContentUseCase` (not a ViewModel; background pipeline)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `server_session_state` | string | `revoked` \| `active` \| `unknown` | Result of the server probe (a token refresh — the refresh token is the session-bound credential, unlike the ~1h-valid access token). `revoked`: the session is really gone server-side → a legitimate remote sign-out. `active`: the session still lives → a **false positive**, the local row vanished for another reason (this is the bug signal). `unknown`: the probe was inconclusive (offline or a 5xx). |

## Notes

- `active` is the value to watch: it is a false-positive revocation the gate just prevented from logging the user out. A sustained rate of it means something is still deleting the local device row while the session lives — the root cause worth fixing at its source.
- The `active` case is also reported to Crashlytics as a non-fatal (`FalsePositiveDeviceRevocationException`) for device-timeline correlation. `revoked` (the healthy remote sign-out) and `unknown` (inconclusive, e.g. offline) are not reported as non-fatals to keep the signal meaningful.
- The path marks the logout as intentional before the probe, so a `revoked` sign-out never double-counts as `session_lost`; on `active`/`unknown` the mark is released since no logout happens.
- On `active`/`unknown` the device's own row stays missing from its local list until the next snapshot pull (on realtime reconnect) or app restart re-registers it; the user is not signed out. Proactively re-registering to restore the row immediately is a possible follow-up.
