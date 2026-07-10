# login_failed

**Tier:** P1 | **Domain:** Auth

Captures a sign-in attempt that ended in an error shown to the user. Splitting failures by provider and reason separates infrastructure problems (connection) from provider-policy problems (Apple hiding the email) and unknown bugs (generic), which have very different fixes.

## When it fires

A sign-in attempt errors out and the login sheet displays an error message. Two code paths lead here.

## Trigger source

`feature/login/.../LoginViewModel.kt` — two trigger points:

- `LoginUiEvent.SocialLoginClick` — `signInStarter` returns failure; the throwable is classified by `ThrowableToLoginErrorMapper`.
- `LoginUiEvent.SocialAuthResult` — result is `NativeSignInResult.NetworkError` (always `connection`) or `NativeSignInResult.Error` (classified by `ThrowableToLoginErrorMapper`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `apple` | Auth provider (`google` \| `apple`) |
| `reason` | string | `email_required` | `LoginError` value: `connection` (timeout/offline) \| `email_required` (provider returned no email, e.g. Apple email scope withheld) \| `generic` |

## Notes

- Plan discrepancy: the plan listed a single trigger ("mapped `LoginError`"); the code has the two entry points above — both must be instrumented.
- `NativeSignInResult.Error` throwables recognized by `NoGoogleAccountClassifier` (no Google account on the device) show the add-account dialog instead of an error message; that recoverable path is **not** a `login_failed` — the user typically retries after adding an account.
- Funnel: [login_started](login_started.md) → `login_failed`; compare with [login_cancelled](login_cancelled.md) to distinguish errors from voluntary abandonment.
