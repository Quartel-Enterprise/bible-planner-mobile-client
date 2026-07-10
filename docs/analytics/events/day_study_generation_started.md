# day_study_generation_started

**Tier:** P1 | **Domain:** DayStudy

Captures the start of an AI day-study generation — the moment backend cost is incurred. Volume by `is_pro` and `remaining_free` shows how the free quota is consumed and how close free users get to the limit that drives Pro conversion.

## When it fires

All pre-flight checks pass (no cached study, device online, free quota allows it) and `DayStudyGenerationCoordinator.start` is invoked, kicking off the streaming generation.

## Trigger source

`feature/day_study/.../presentation/viewmodel/DayStudyViewModel.kt` — `DayStudyUiEvent.OnCardClick` → `startGenerationOrCachedOpen()` at the `generationCoordinator.start(...)` call

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Reading plan of the day being studied |
| `week_number` | int | `12` | 1-based week within the plan |
| `day_number` | int | `3` | 1-based day within the week |
| `is_pro` | boolean | `false` | Whether the user has the Pro entitlement |
| `remaining_free` | int | `2` | `DayStudyQuotaModel.remainingFree` at start time (free generations left) |

## Notes

- Not fired when a cached study exists (that opens directly — see [day_study_opened](day_study_opened.md) with `is_cached=true`), when offline, or when the quota/concurrency check blocks the start (see [day_study_generation_failed](day_study_generation_failed.md)).
- Generation is app-scoped (`DayStudyGenerationCoordinator`), so it keeps running if the user leaves the day screen; completion may be observed on a later visit or via the global floating card.
- `plan_type`/`week_number`/`day_number` come from the `DayNavRoute` held by the ViewModel.
