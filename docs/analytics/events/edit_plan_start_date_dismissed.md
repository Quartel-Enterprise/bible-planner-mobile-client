# edit_plan_start_date_dismissed

**Tier:** P2 | **Domain:** Settings

Captures the user closing the edit-plan-start-date dialog without selecting a new date. Complements [plan_start_date_changed](plan_start_date_changed.md) to show how often the dialog is opened and dismissed without a change.

## When it fires

User dismisses the edit-plan-start-date dialog (scrim tap, cancel action, or system back).

## Trigger source

`feature/preferences/edit_plan_start_date/src/commonMain/kotlin/com/quare/bibleplanner/feature/editplanstartdate/presentation/viewmodel/EditPlanStartDateViewModel.kt` — `EditPlanStartDateUiEvent.OnDismissDialog`

## Parameters

None.

## Notes

- Destination impression for the dialog itself is covered by [destination_view](destination_view.md) (`edit_plan_start_date`, `dialog`).
- Related: [plan_start_date_changed](plan_start_date_changed.md).
