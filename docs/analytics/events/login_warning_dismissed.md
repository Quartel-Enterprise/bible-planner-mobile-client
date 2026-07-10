# login_warning_dismissed

**Tier:** P2 | **Domain:** Auth

Captures the user closing the login-warning dialog without signing in — declining the gated action rather than creating an account. A high dismissal rate for a given `reason` means that gate loses users instead of converting them.

## When it fires

The user dismisses the login-warning dialog (close button, scrim tap or back).

## Trigger source

`feature/login_warning/.../LoginWarningViewModel.kt` — `LoginWarningUiEvent.OnDismiss`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `preferences_theme` | `LoginWarningReason` key of the gated action: `purchase` \| `day_study` \| `preferences_theme` \| `preferences_language` |

## Notes

- Together with [login_warning_accepted](login_warning_accepted.md) this exhausts the outcomes of [login_warning_shown](login_warning_shown.md) — the dialog has no third path.
- Not a `*_cancelled` name on purpose: nothing was started; the user is dismissing an informational gate.
