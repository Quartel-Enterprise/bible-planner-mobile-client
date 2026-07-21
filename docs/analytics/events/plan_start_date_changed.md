# plan_start_date_changed

**Tier:** P1 | **Domain:** Reading

Captures editing the plan start date. Signals users re-anchoring their schedule (starting over, catching up after a break) — a churn-risk and re-engagement marker.

## When it fires

User confirms a new date in the edit-plan-start-date dialog.

## Trigger source

`feature/preferences/edit_plan_start_date/src/commonMain/kotlin/com/quare/bibleplanner/feature/editplanstartdate/presentation/viewmodel/EditPlanStartDateViewModel.kt` — `EditPlanStartDateUiEvent.OnDateSelected`

The dialog is opened from two places:

- `feature/reading_plan/.../presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnEditPlanClick` and `ReadingPlanUiEvent.OnOverflowOptionClick` (`OverflowOption.EDIT_START_DAY`) → `source=plan_screen`
- `feature/profile/.../presentation/viewmodel/ProfileViewModel.kt` — profile-menu item → `source=profile_menu`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `plan_screen` \| `profile_menu` | Where the edit dialog was opened from |

## Notes

- Discrepancy with the plan table: `EditPlanStartDateNavRoute` is a `data object` with no arguments, so the dialog's ViewModel cannot know its caller. To log `source`, either add a source argument to the route or log the event caller-side; until then the parameter cannot be populated from inside the dialog.
- Dismissing the dialog without picking a date (`OnDismissDialog`) is intentionally not tracked (picker dismissals are excluded, see README).
- Dialog impressions are covered by [destination_view](destination_view.md) (`edit_plan_start_date`).
