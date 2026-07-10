# plan_group_toggled

**Tier:** P2 | **Domain:** Books

Captures the user expanding or collapsing the "upcoming" or "completed" week groups on the reading-plan screen. Shows whether users review finished weeks or peek ahead, beyond the currently active section.

## When it fires

User taps the upcoming-weeks or completed-weeks group header on the reading-plan screen.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — two trigger points:

- `ReadingPlanUiEvent.OnToggleUpcomingExpanded`
- `ReadingPlanUiEvent.OnToggleCompletedExpanded`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `group` | string | `completed` | Which group was toggled: `upcoming` \| `completed` |
| `is_expanded` | boolean | `true` | Group state after the toggle. Both UiEvents are parameterless `data object`s; the new state is read from the updated UiState |

## Notes

- Related: [plan_week_toggled](plan_week_toggled.md), [plan_shortcut_used](plan_shortcut_used.md).
