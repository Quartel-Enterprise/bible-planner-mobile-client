# plan_overflow_option_clicked

**Tier:** P2 | **Domain:** Books

Captures which option the user picks from the reading-plan screen's overflow menu, showing how often users reach for plan-management actions (changing the start day, deleting progress) from that entry point.

## When it fires

User taps an option in the reading-plan overflow menu.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnOverflowOptionClick(option: OverflowOption)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `option` | string | `edit_start_day` | `OverflowOption` value lowercased to snake_case: `edit_start_day` \| `delete_progress` |

## Notes

- Opening/closing the overflow menu itself (`OnOverflowClick`, `OnOverflowDismiss`) is not tracked (see README "Explicitly not tracked").
- `edit_start_day` navigates to `EditPlanStartDateNavRoute` and `delete_progress` to `DeleteAllProgressNavRoute`; those destination impressions are covered by [destination_view](destination_view.md).
- Related: [plan_edit_clicked](plan_edit_clicked.md), [plan_shortcut_used](plan_shortcut_used.md).
