# day_reading_complete_shown

**Tier:** P1 | **Domain:** DayReadingComplete

Captures every time the day-reading-complete celebration sheet appears, split by how the day's
timing compared to schedule and by the reader's account/quota state. Entry point of a secondary
AI-study funnel (alongside the existing Day-screen card) and a signal of how often readers actually
finish a full plan day.

## When it fires

The sheet is shown, right after the reading screen marks the last unread chapter of a reading-plan
day as read (in any order).

## Trigger source

`feature/day_reading_complete/.../presentation/viewmodel/DayReadingCompleteViewModel.kt` — fires once
the day's data and account/quota state finish loading.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | `chronological` \| `books` |
| `week_number` | int | `1` | The completed day's week number |
| `day_number` | int | `1` | The completed day's day number |
| `timing` | string | `on_time` | `on_time` \| `overdue` \| `early`, from `DayTimingState` |
| `account_state` | string | `free` | `free` \| `free_exhausted` \| `pro` |
| `chapter_count` | int | `3` | Total chapters across the day's passages |

## Notes

- Destination impression is also covered by [screen_view](screen_view.md) (`day_reading_complete`,
  `bottom_sheet`); this event exists separately because `screen_view` doesn't carry `timing` or
  `account_state`.
- Funnel: `day_reading_complete_shown` → [day_reading_complete_cta_clicked](day_reading_complete_cta_clicked.md)
  or [day_reading_complete_dismissed](day_reading_complete_dismissed.md).
