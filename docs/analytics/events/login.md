# login

**Tier:** P1 | **Domain:** Auth

Captures a returning user signing back in — an auth success for an account that already existed. Measures reactivation of existing account holders and which provider they choose. Uses the **GA4 standard event name** `login` with the standard `method` parameter, so Firebase's built-in login reports work out of the box.

First-time account creation is reported as [sign_up](sign_up.md) instead, not as `login`.

## When it fires

The social sign-in flow completes successfully — the provider round-trip returns `NativeSignInResult.Success` and Supabase has a session — **and** the account already existed.

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.SocialAuthResult` with `NativeSignInResult.Success`, when `IsNewAccount` returns `false`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `google` | Auth provider (`google` \| `apple`), from `LoginUiEvent.SocialAuthResult.provider` (`LoginProvider`) |

## Notes

- Funnel: [login_started](login_started.md) → `login`, with [login_failed](login_failed.md) and [login_cancelled](login_cancelled.md) as the drop-off branches.
- **Mutually exclusive with [sign_up](sign_up.md)**: one auth success emits exactly one of the two, never both. Counts before the split are not comparable with counts after it — `login` alone used to mean every success, and now excludes account creation; sum both to reproduce the old series.
- The bottom sheet also auto-closes when `ObserveAuthenticatedUserId` emits a user id; log only on the `Success` result, not on the close, to avoid double counting.
- Success also kicks off the snapshot pull — see [sync_completed](sync_completed.md).
