# plan_edit_clicked

**Tier:** P2 | **Domain:** Books

Captures the user tapping the edit action on the reading-plan screen to change the plan's start date, a shortcut to the same destination reachable from the overflow menu's `edit_start_day` option.

## When it fires

User taps the edit-plan action on the reading-plan screen.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnEditPlanClick`

## Parameters

None.

## Notes

- Navigates to `EditPlanStartDateNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`edit_plan_start_date`, `dialog`).
- Related: [plan_overflow_option_clicked](plan_overflow_option_clicked.md) (`edit_start_day` option reaches the same destination via a different entry point), [plan_start_date_changed](plan_start_date_changed.md).
