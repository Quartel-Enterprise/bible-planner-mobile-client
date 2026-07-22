# day_study_load

**Tier:** P1 | **Domain:** DayStudy

Performance event: measures how long the user waits between arriving at a surface and seeing the AI study entry loaded (out of its shimmer skeleton). Segmenting `duration_ms` by `is_cached` gives the before/after picture for load-time optimizations across the real device population.

## When it fires

Once per surface exhibition, on the first transition of the entry card out of `Loadable.Loading`:

- `target=card` — the day screen's study card (`DayStudySection`); the mark starts when `DayStudyUiEvent.OnStart` arrives for a new day route.
- `target=panel` — the day-study route screen's entry panel; the mark starts when `DayStudyRouteViewModel` is created.

On a quota-load failure the event still fires with `success=false` (the surface stays on its skeleton).

## Trigger source

`feature/day_study/.../presentation/viewmodel/DayStudyViewModel.kt` (`target=card`) and `feature/day_study/.../presentation/viewmodel/DayStudyRouteViewModel.kt` (`target=panel`) — `trackLoad()`, called from `refreshCard()`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `target` | string | `card` | `card` (day screen) or `panel` (day-study route screen) |
| `duration_ms` | int | `2870` | Elapsed monotonic time from surface start to loaded (or failed) state |
| `success` | boolean | `true` | `false` when loading the quota threw instead of producing a card |
| `is_cached` | boolean | `true` | Whether the study already existed in the local Room cache |
| `is_pro` | boolean | `false` | Whether the user has the Pro entitlement |
| `reason` | string | `IllegalStateException` | Only on `success=false`: the failure exception's simple class name, or `unknown` |

## Notes

- Fired at most once per exhibition: the time mark is consumed on first fire, so later quota refreshes (pro-status changes, generation completions) don't re-emit.
- Analyze `duration_ms` percentiles (p50/p90) split by `is_cached` and `target`.
