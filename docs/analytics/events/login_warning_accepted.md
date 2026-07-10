# login_warning_accepted

**Tier:** P1 | **Domain:** Auth

Captures the user tapping the login button on the login-warning dialog. Split by `reason`, it shows which gated feature is the strongest motivator to sign in — e.g. whether purchase intent converts to login better than Day Study curiosity.

## When it fires

The user taps the sign-in button on the login-warning dialog; the app navigates to the login bottom sheet.

## Trigger source

`feature/login_warning/.../LoginWarningViewModel.kt` — `LoginWarningUiEvent.OnLoginClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `day_study` | `LoginWarningReason` key of the gated action: `purchase` \| `day_study` \| `preferences_theme` \| `preferences_language` |

## Notes

- Like the nudge, acceptance only opens the login sheet — actual sign-in completion is [login](login.md); the full funnel is [login_warning_shown](login_warning_shown.md) → `login_warning_accepted` → [login_started](login_started.md) → [login](login.md).
- Counterpart: [login_warning_dismissed](login_warning_dismissed.md).
