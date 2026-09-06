# login_failed

**Tier:** P1 | **Domain:** Auth

Captures a sign-in attempt that ended in an error shown to the user. Splitting failures by provider and reason separates infrastructure problems (connection) from provider-policy problems (Apple hiding the email) and unknown bugs (generic), which have very different fixes.

## When it fires

A sign-in attempt errors out and the login sheet displays an error message. Two code paths lead here.

## Trigger source

`feature/login/.../LoginViewModel.kt` — two trigger points:

- `LoginUiEvent.SocialLoginClick` — `signInStarter` returns failure; the throwable is classified by `ThrowableToLoginErrorMapper`.
- `LoginUiEvent.SocialAuthResult` — result is `NativeSignInResult.NetworkError` (always `connection`) or `NativeSignInResult.Error` (`google_unavailable` when `IsGoogleCredentialUnavailable` recognizes it, otherwise classified by `ThrowableToLoginErrorMapper`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `apple` | Auth provider (`google` \| `apple`) |
| `reason` | string | `email_required` | `LoginError` value: `connection` (timeout/offline) \| `email_required` (provider returned no email, e.g. Apple email scope withheld) \| `google_unavailable` (Android Credential Manager issued no credential although the device has a Google account — usually an OAuth client that does not cover this build) \| `generic` |

## Notes

- Plan discrepancy: the plan listed a single trigger ("mapped `LoginError`"); the code has the two entry points above — both must be instrumented.
- `google_unavailable` shows the Google-unavailable dialog (with an "add account" shortcut) instead of an inline error message, and no snackbar. It is still a `login_failed`: since Android 8.0 the app cannot read the device accounts, so a missing account and a rejected request are indistinguishable — and a rise in this reason is the signal that the OAuth client stopped matching a build.
- Funnel: [login_started](login_started.md) → `login_failed`; compare with [login_cancelled](login_cancelled.md) to distinguish errors from voluntary abandonment.
