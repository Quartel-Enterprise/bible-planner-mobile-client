# day_study_login_required_clicked

**Tier:** P2 | **Domain:** DayStudy

Captures the user interacting with the Day Study card while signed out, triggering the login-required warning. This click also triggers navigation to the login-warning dialog (which `destination_view` logs separately), so this event isolates click-through intent from the resulting screen-view.

## When it fires

A signed-out user taps the Day Study card/action on the Day screen, opening the login-warning dialog with reason `day_study`.

## Trigger source

`feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/model/DayUiEvent.kt` — `DayUiEvent.OnDayStudyLoginRequired`

## Parameters

None.

## Notes

- Navigates to `LoginWarningNavRoute` with `reason = day_study`. Related: [destination_view](destination_view.md) (`login_warning` with `reason`) fires separately once the dialog becomes visible; [login_warning_shown](login_warning_shown.md), [login_warning_accepted](login_warning_accepted.md), and [login_warning_dismissed](login_warning_dismissed.md) cover the resulting funnel.
