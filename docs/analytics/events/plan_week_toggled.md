# plan_week_toggled

**Tier:** P2 | **Domain:** Books

Captures the user expanding or collapsing a week card on the reading-plan screen. Shows how users scan the plan — whether they browse ahead/behind or stay on the auto-expanded current week.

## When it fires

User taps a week header on the reading-plan screen.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnWeekExpandClick(weekNumber: Int)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `week_number` | int | `12` | Standard param; the toggled week |
| `is_expanded` | boolean | `true` | Week state after the toggle. The UiEvent carries only `weekNumber`; the new state is read from the updated UiState |

## Notes

- Programmatic expansion (auto-expanding the active week) does not fire this event — only explicit taps.
- Related: [plan_group_toggled](plan_group_toggled.md), [plan_shortcut_used](plan_shortcut_used.md), [day_read_toggled](day_read_toggled.md).
