# sign_up

**Tier:** P1 | **Domain:** Auth

Captures the creation of an account: a social sign-in that succeeded for a user who did not exist before. Uses the **GA4 standard event name** `sign_up` with the standard `method` parameter, so it is eligible as a key event and can be imported into Google Ads as an acquisition conversion for app install campaigns.

Splits the auth funnel's success branch in two: `sign_up` is acquisition, [login](login.md) is a returning user coming back. Before this event existed both were reported as `login`, so new and returning users were indistinguishable.

## When it fires

The social sign-in flow completes successfully — the provider round-trip returns `NativeSignInResult.Success` and Supabase has a session — **and** that session created the account.

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.SocialAuthResult` with `NativeSignInResult.Success`, when `IsNewAccount` returns `true`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `google` | Auth provider (`google` \| `apple`), from `LoginUiEvent.SocialAuthResult.provider` (`LoginProvider`) |

## Notes

- **Mutually exclusive with [login](login.md)**: one auth success emits exactly one of the two, never both, so the pair still sums to the total success count of the funnel and neither double-counts the other.
- How "new" is decided: `IsNewAccountUseCase` compares the user record's `created_at` and `last_sign_in_at`. Supabase writes both in the same transaction when it creates the account, so they are a moment apart on the first sign-in and at least a session apart on every later one; the use case allows a 10-second window between them.
- The session source deliberately does **not** drive this. `SessionSource.SignUp` describes only Supabase's email/password `signUpWith` flow, and the native Google/Apple sign-in this app uses always reports `SessionSource.SignIn` whether or not the account already existed.
- When either timestamp is missing the event falls back to `login`, so an unprovable sign-up never inflates acquisition.
- Funnel: [login_started](login_started.md) → `sign_up`, with [login_failed](login_failed.md) and [login_cancelled](login_cancelled.md) as the drop-off branches.
