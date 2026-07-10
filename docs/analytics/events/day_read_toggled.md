# day_read_toggled

**Tier:** P1 | **Domain:** Reading

Captures marking a whole plan day as read (or unread). Together with [chapter_read_toggled](chapter_read_toggled.md) it measures how users actually progress: day-level bulk checks vs chapter-by-chapter reading.

## When it fires

User taps the read checkbox on a day row in the reading plan list, or taps the day-level read toggle on the Day screen.

## Trigger source

- `feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnDayReadClick` (`source=plan_list`)
- `feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/viewmodel/DayViewModel.kt` — `DayUiEvent.OnDayReadToggle` (`source=day_screen`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Active reading plan |
| `week_number` | int | `12` | 1-based week within the plan |
| `day_number` | int | `3` | 1-based day within the week |
| `is_read` | boolean | `true` | New read status after the toggle |
| `source` | string | `plan_list` \| `day_screen` | Which surface triggered the toggle |

## Notes

- Marking a day read marks all its passages read; do not additionally fire [chapter_read_toggled](chapter_read_toggled.md) per chapter — this event covers the bulk action.
- If the toggle completes the week, [week_completed](week_completed.md) fires; if it completes the plan, [plan_completed](plan_completed.md).
- The plan list applies an optimistic override (`pendingReadOverrides`) before persistence; log on the user action, not on flow reconciliation, to avoid duplicates.
