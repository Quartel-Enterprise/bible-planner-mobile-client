# login_cancelled

**Tier:** P2 | **Domain:** Auth

Captures a sign-in attempt the user abandoned by closing the native provider sheet. A high cancel rate on one provider suggests friction in that provider's native flow (account picker confusion, unexpected permission asks) rather than a technical failure.

## When it fires

The user dismisses the native Google/Apple sign-in sheet without completing it.

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.SocialAuthResult` with `NativeSignInResult.ClosedByUser`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `google` | Auth provider (`google` \| `apple`) |

## Notes

- No error message is shown for this result (the ViewModel only clears the loading state), so this event is the only trace of the abandonment.
- Dismissing the login **bottom sheet** itself (`DismissClick` / `NotNowClick`) is not tracked as a cancel — no native flow was started; the sheet impression is covered by `destination_view`.
- Funnel: [login_started](login_started.md) → `login_cancelled`.
