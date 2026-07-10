# chapter_read_toggled

**Tier:** P1 | **Domain:** Reading

Captures the atomic unit of reading progress: marking a single chapter as read (or unread). This is the highest-frequency progress event and feeds every derived completion metric (book, week, plan, Bible progress).

## When it fires

User taps the read toggle in the chapter reader, or taps a chapter checkbox on the Day screen.

## Trigger source

- `feature/read/src/commonMain/kotlin/com/quare/bibleplanner/feature/read/presentation/ReadViewModel.kt` — `ReadUiEvent.ToggleReadStatus` (`source=reader`)
- `feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/viewmodel/DayViewModel.kt` — `DayUiEvent.OnChapterCheckboxClick` (`source=day_screen`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `genesis` | Book the chapter belongs to |
| `chapter_number` | int | `5` | 1-based chapter within the book |
| `is_read` | boolean | `true` | New read status after the toggle |
| `source` | string | `reader` \| `day_screen` | Which surface triggered the toggle |
| `plan_type` | string | `chronological` | Only when `source=day_screen` |
| `week_number` | int | `12` | Only when `source=day_screen` |
| `day_number` | int | `3` | Only when `source=day_screen` |

## Notes

- On the Day screen the checkbox carries an `UpdateReadStatusOfPassageStrategy`: the `Chapter` strategy toggles one chapter, but the `EntireBook` strategy toggles a whole single-book passage at once — log one event per chapter affected, or treat the `EntireBook` case as [book_read_toggled](book_read_toggled.md) instead of inflating chapter counts.
- A toggle that completes a book must also fire [book_completed](book_completed.md); it can cascade into [week_completed](week_completed.md), [plan_completed](plan_completed.md) and [bible_progress_milestone](bible_progress_milestone.md).
- Do not log the failure path (`toggleChapterReadStatus().onFailure` shows a snackbar and rolls back).
