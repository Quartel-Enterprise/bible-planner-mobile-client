# plan_shortcut_used

**Tier:** P2 | **Domain:** Books

Captures the user tapping one of the reading-plan navigation shortcuts: jump to the active row, skip to today, or scroll back to top. Shortcut usage validates these affordances and shows how far users stray from their current position in the plan.

## When it fires

User taps a shortcut button on the reading-plan screen.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — three trigger points:

- `ReadingPlanUiEvent.OnGoToActiveRowClick`
- `ReadingPlanUiEvent.OnSkipToTodayClick`
- `ReadingPlanUiEvent.OnScrollToTopClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `shortcut` | string | `today` | Which shortcut was used: `active_row` \| `today` \| `scroll_top` |

## Notes

- Scroll-completion bookkeeping (`OnScrollToWeekCompleted`, `OnScrollToTopCompleted`, `OnFlashCompleted`, `OnScrollStateChange`, `OnActiveRowVisibilityChange`) is not tracked (see README "Explicitly not tracked").
- Related: [plan_week_toggled](plan_week_toggled.md), [plan_group_toggled](plan_group_toggled.md).
