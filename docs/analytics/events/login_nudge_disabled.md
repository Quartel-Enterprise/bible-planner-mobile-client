# login_nudge_disabled

**Tier:** P1 | **Domain:** Auth

Captures the user permanently opting out of the login-sync nudge via the "don't show again" checkbox. This is hard rejection — these users can never be nudged again, so the rate directly bounds the nudge's future reach and is the key signal that the dialog annoys people.

## When it fires

The nudge dialog closes (any way: login, "not now" or dismiss) while the "don't show again" checkbox is checked; `DismissLoginNudgePermanently` persists the opt-out.

## Trigger source

`feature/login_sync_nudge/.../LoginSyncNudgeViewModel.kt` — the `close()` path with `dontShowAgain=true`, reached from `LoginSyncNudgeUiEvent.OnLoginClick`, `LoginSyncNudgeUiEvent.OnNotNow` or `LoginSyncNudgeUiEvent.OnDismiss` (checkbox state comes from `LoginSyncNudgeUiEvent.OnDontShowAgainToggled`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Plan discrepancy: the plan framed this as the permanent-dismiss action only, but in code the permanent opt-out applies on **any** close while the checkbox is checked — including tapping login. So this event can co-occur with [login_nudge_accepted](login_nudge_accepted.md), and when it fires on a dismissal it **replaces** [login_nudge_snoozed](login_nudge_snoozed.md).
- Checkbox toggling itself (`OnDontShowAgainToggled`) is not tracked; only the persisted outcome matters.
- Related: [login_nudge_shown](login_nudge_shown.md).
