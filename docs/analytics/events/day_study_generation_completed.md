# day_study_generation_completed

**Tier:** P1 | **Domain:** DayStudy

Captures a successfully finished AI day-study generation. The completed/started ratio is the reliability KPI of the AI feature; a drop points at backend or streaming issues before users start complaining.

## When it fires

The generation stream emits `DayStudyGenerationEventModel.Completed` and the coordinator marks the job `DayStudyGenerationStatus.Done`.

## Trigger source

`feature/day_study/.../domain/coordinator/DayStudyGenerationCoordinator.kt` — `start(...)`, the `Completed` branch that sets `DayStudyGenerationStatus.Done` (log here so completion is captured even when the user has left the day screen)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Reading plan of the studied day |
| `week_number` | int | `12` | 1-based week within the plan |
| `day_number` | int | `3` | 1-based day within the week |
| `is_pro` | boolean | `false` | Whether the user has the Pro entitlement |

## Notes

- Always a fresh generation: cached studies never enter the coordinator (`hasCachedStudy` short-circuits before `start`). The planning doc's "cached vs fresh" distinction therefore lives on [day_study_opened](day_study_opened.md) (`is_cached`), not here — a constant `is_cached=false` param would be noise.
- Pairs 1:1 with [day_study_generation_started](day_study_generation_started.md); the failure counterpart is [day_study_generation_failed](day_study_generation_failed.md).
- Completion while the day is backgrounded surfaces through the global floating card; the eventual sheet open still logs [day_study_opened](day_study_opened.md).
