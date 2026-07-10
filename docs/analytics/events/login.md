# login

**Tier:** P1 | **Domain:** Auth

Captures a successful sign-in. This is the conversion point of the whole auth funnel: it measures how many logged-out users become account holders (unlocking sync, purchases and Day Study) and which provider they choose. Uses the **GA4 standard event name** `login` with the standard `method` parameter, so Firebase's built-in login reports work out of the box.

## When it fires

The social sign-in flow completes successfully — the provider round-trip returns `NativeSignInResult.Success` and Supabase has a session.

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.SocialAuthResult` with `NativeSignInResult.Success`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `google` | Auth provider (`google` \| `apple`), from `LoginUiEvent.SocialAuthResult.provider` (`LoginProvider`) |

## Notes

- Funnel: [login_started](login_started.md) → `login`, with [login_failed](login_failed.md) and [login_cancelled](login_cancelled.md) as the drop-off branches.
- The bottom sheet also auto-closes when `ObserveAuthenticatedUserId` emits a user id; log only on the `Success` result, not on the close, to avoid double counting.
- Success also kicks off the snapshot pull — see [sync_completed](sync_completed.md).
