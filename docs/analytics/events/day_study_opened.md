# day_study_opened

**Tier:** P2 | **Domain:** DayStudy

Captures the AI study sheet actually being opened with content — the consumption side of the AI feature, as opposed to the generation side. The cached share shows how much value each generated study delivers beyond its first read.

## When it fires

The study sheet opens showing a study: a cached study is opened directly, a fresh generation finishes while the sheet is open, or the user reopens an already-loaded study by tapping the card.

## Trigger source

`feature/day_study/.../presentation/viewmodel/DayStudyViewModel.kt` — three paths:

- `openCachedStudy()` (cached study opened via `DayStudyUiEvent.OnCardClick`) — `is_cached=true`
- `onJobDone(...)` when `isStudyOpen` is true (fresh generation completing into the open sheet) — `is_cached=false`
- `DayStudyUiEvent.OnCardClick` early-return reopen when `openStudy != null` — `is_cached=true`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_cached` | boolean | `true` | `true` when the study came from cache / was already loaded; `false` when a generation just produced it |

## Notes

- This carries the "cached vs fresh" split the planning doc originally placed on [day_study_generation_completed](day_study_generation_completed.md); it lives here because generations are always fresh.
- Reopening the sheet repeatedly in one session logs multiple events; dedupe per day-route in analysis if needed.
- Sheet dismissals (`OnStudyDismiss`) are deliberately not tracked (UI bookkeeping).
