# login_nudge_dont_show_again_toggled

**Tier:** P2 | **Domain:** Auth

Captures the user toggling the "don't show again" checkbox on the login-sync nudge, independent of which button they eventually tap.

## When it fires

User taps the "don't show again" checkbox on the login-sync nudge dialog.

## Trigger source

`feature/login_sync_nudge/.../LoginSyncNudgeViewModel.kt` — `LoginSyncNudgeUiEvent.OnDontShowAgainToggled(isChecked: Boolean)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | boolean | `true` | Whether the checkbox is now checked |

## Notes

- The checkbox's value at confirm/dismiss time is also reflected in whether [login_nudge_disabled](login_nudge_disabled.md) fires alongside [login_nudge_accepted](login_nudge_accepted.md)/[login_nudge_snoozed](login_nudge_snoozed.md) — this event isolates the toggle interaction itself.
