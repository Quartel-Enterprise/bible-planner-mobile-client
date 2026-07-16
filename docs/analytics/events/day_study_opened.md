# day_study_opened

**Tier:** P2 | **Domain:** DayStudy

Captures the AI study sheet actually being opened with content — the consumption side of the AI feature, as opposed to the generation side. The cached share shows how much value each generated study delivers beyond its first read.

## When it fires

The day-study route shows a study: a cached study is opened on entering the route, or a fresh generation finishes while the route is open.

## Trigger source

`feature/day_study/.../presentation/viewmodel/DayStudyRouteViewModel.kt` — two paths:

- `openCachedStudy()` (cached study opened via `DayStudyRouteUiEvent.OnCardClick`) — `is_cached=true`
- `onJobDone(...)` (fresh generation completing while the route is open) — `is_cached=false`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_cached` | boolean | `true` | `true` when the study came from cache / was already loaded; `false` when a generation just produced it |

## Notes

- This carries the "cached vs fresh" split the planning doc originally placed on [day_study_generation_completed](day_study_generation_completed.md); it lives here because generations are always fresh.
- Reopening the day-study route repeatedly in one session logs multiple events; dedupe per day-route in analysis if needed.
