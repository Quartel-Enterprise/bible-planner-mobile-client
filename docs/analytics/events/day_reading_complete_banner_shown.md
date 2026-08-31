# day_reading_complete_banner_shown

**Tier:** P1 | **Domain:** DayReadingComplete

Captures the discreet banner variant of the day-complete celebration appearing over the reader — the non-blocking alternative to [day_reading_complete_shown](day_reading_complete_shown.md).

## When it fires

The user marks the day's last scheduled chapter as read while the study suggestion is enabled in banner mode, and the banner resolves its call to action for the first time.

## Trigger source

`feature/day_reading_complete/.../presentation/viewmodel/DayReadingCompleteBannerViewModel.kt` — `trackShownOnce`, called from `showCta` after the quota answers (prefetched or fresh); tracked at most once per banner.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Reading plan the finished day belongs to |
| `week_number` | int | `1` | Week of the finished day |
| `day_number` | int | `2` | Day within the week |
| `timing` | string | `on_time` | `on_time`, `overdue` or `early` relative to the planned date |
| `account_state` | string | `free` | `free`, `free_exhausted` or `pro` — what the CTA offered |
| `chapter_count` | int | `3` | Chapters scheduled for the finished day |

## Notes

- Mirrors [day_reading_complete_shown](day_reading_complete_shown.md) parameter-for-parameter so the two surfaces compare directly.
- Like the sheet, what is tracked is what the reader actually saw first — a prefetched quota answer counts.
