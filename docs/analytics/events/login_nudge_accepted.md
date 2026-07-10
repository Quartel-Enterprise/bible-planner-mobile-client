# login_nudge_accepted

**Tier:** P1 | **Domain:** Auth

Captures the user tapping the login button on the login-sync nudge. This is the nudge's conversion click and validates the whole mechanism: the nudge exists to turn engaged logged-out users into account holders at the moment their progress becomes worth backing up.

## When it fires

The user taps the sign-in button on the login-sync nudge dialog; the app navigates to the login bottom sheet.

## Trigger source

`feature/login_sync_nudge/.../LoginSyncNudgeViewModel.kt` — `LoginSyncNudgeUiEvent.OnLoginClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Acceptance only opens the login sheet — completion of the sign-in shows up as [login](login.md) (nudge → [login_started](login_started.md) → [login](login.md)).
- If the "don't show again" checkbox is checked when the user taps login, `DismissLoginNudgePermanently` also runs, so [login_nudge_disabled](login_nudge_disabled.md) fires alongside this event.
- Funnel numerator over [login_nudge_shown](login_nudge_shown.md).
