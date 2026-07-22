# day_study_generation_time

**Tier:** P1 | **Domain:** DayStudy

Performance event: measures the wall-clock duration of an AI study generation, from the moment the coordinator starts the job to its terminal state. Complements [day_study_generation_completed](day_study_generation_completed.md) / [day_study_generation_failed](day_study_generation_failed.md), which carry the funnel context but no timing.

## When it fires

Once per generation job, when it reaches a terminal state:

- generation stream completes → `success=true`
- generation fails → `success=false` with `reason`

## Trigger source

`feature/day_study/.../domain/coordinator/DayStudyGenerationCoordinator.kt` — `trackGenerationTime()`, called from the `Completed` event branch and the `onFailure` branch of `start()`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `duration_ms` | int | `18450` | Elapsed monotonic time from `start()` to the terminal state |
| `success` | boolean | `true` | Whether the generation produced a study |
| `is_pro` | boolean | `false` | Whether the user has the Pro entitlement |
| `reason` | string | `limit_reached` | Only on `success=false`: `limit_reached` or `error` |
| `reading_ms` | int | `2100` | Time spent in the `READING` phase (chapter aggregation before the model streams) |
| `chapters_ms` | int | `9400` | Time from the `CHAPTERS` phase starting until the next phase |
| `context_ms` | int | `5300` | Time from the `CONTEXT` phase starting until the next phase |
| `questions_ms` | int | `7800` | Time from the `QUESTIONS` phase starting until the terminal state |

## Notes

- Measured in the app-scoped coordinator, so the duration survives the user leaving the day screen mid-generation and fires exactly once regardless of how many ViewModels observe the job.
- Phase segments are derived from the SSE `PhaseChanged` events: each `*_ms` param covers the span from that phase's first report until the next phase (or the terminal state for the last one). A phase param is absent when the phase was never reported (e.g. failures before streaming started).
- `duration_ms` here is dominated by the backend SSE stream (chapter aggregation + Gemini generation), unlike [day_study_load](day_study_load.md) which measures surface readiness.
