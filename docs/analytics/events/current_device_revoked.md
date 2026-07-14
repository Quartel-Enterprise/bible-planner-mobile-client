# current_device_revoked

**Tier:** P1 | **Domain:** Auth

Captures this device detecting that its own row disappeared from the synced connected-devices list, which makes the app end the local session (the remote sign-out flow introduced in #255). The detection is based on local Room state, so this event measures how often the path fires — legitimately (someone used "sign out device" on another device) or as a false positive (the local row vanished for any other reason). Compare its rate with `device_signed_out` (the sender side): sustained excess here means false positives are logging users out.

## When it fires

`ObserveCurrentDeviceRevoked` emits (the current device's row disappeared from the local store after having been present) and right before `EndSession` runs.

## Trigger source

`shared/.../domain/usecase/impl/InitializeAppContentUseCase.kt` — `onCurrentDeviceRevoked` (not a ViewModel; background pipeline)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Also reported to Crashlytics as a non-fatal (`CurrentDeviceRevokedException`) for device-timeline correlation with `session_lost` and supabase-kt logs.
- The follow-up `EndSession` marks the logout as intentional, so this path never double-counts as `session_lost`.
