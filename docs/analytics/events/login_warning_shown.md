# login_warning_shown

**Tier:** P1 | **Domain:** Auth

Captures an impression of the login-warning dialog — shown when a logged-out user tries an action that requires an account (purchasing, generating a Day Study, or enabling a synced preference). The `reason` breakdown shows which gated features push users toward creating an account.

## When it fires

The login-warning dialog appears after a logged-out user attempts a login-gated action; the triggering feature navigates to `LoginWarningNavRoute` carrying the reason.

## Trigger source

`feature/login_warning/.../LoginWarningViewModel.kt` — ViewModel creation for `LoginWarningNavRoute` (no `UiEvent`; the `reason` is parsed from the route via `LoginWarningReason.fromKey`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `purchase` | `LoginWarningReason` key: `purchase` \| `day_study` \| `preferences_theme` \| `preferences_language` |

## Notes

- Overlaps with `destination_view` (`destination_name=login_warning`, which also carries `reason` as a route arg); this dedicated event exists so the auth funnel can be built without joining on screen views.
- New `LoginWarningReason` cases automatically become new `reason` values — keep this list in sync with the sealed interface in `core/model/.../loginwarning/LoginWarningReason.kt`.
- Outcomes: [login_warning_accepted](login_warning_accepted.md), [login_warning_dismissed](login_warning_dismissed.md).
