# login_nudge_snoozed

**Tier:** P2 | **Domain:** Auth

Captures the user deferring the login-sync nudge ("not now" or dismissing the dialog) without disabling it. Measures soft rejection: these users stay eligible for a later nudge, so the snooze rate calibrates how aggressive the nudge cadence should be.

## When it fires

The user taps "not now" or dismisses the nudge dialog while the "don't show again" checkbox is unchecked; `SnoozeLoginNudge` records the snooze.

## Trigger source

`feature/login_sync_nudge/.../LoginSyncNudgeViewModel.kt` — `LoginSyncNudgeUiEvent.OnNotNow` and `LoginSyncNudgeUiEvent.OnDismiss` (both funnel into the same close path)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Only fires when the checkbox is unchecked — with it checked, the same clicks run `DismissLoginNudgePermanently` instead and emit [login_nudge_disabled](login_nudge_disabled.md), not this event.
- `OnNotNow` and `OnDismiss` are intentionally collapsed into one event (same user meaning); split with a `source` param later only if the distinction proves useful.
- Related: [login_nudge_shown](login_nudge_shown.md), [login_nudge_accepted](login_nudge_accepted.md).
