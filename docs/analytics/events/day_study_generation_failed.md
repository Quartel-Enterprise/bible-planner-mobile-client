# day_study_generation_failed

**Tier:** P1 | **Domain:** DayStudy

Captures AI study generations that fail or are blocked before starting, split by reason. `limit_reached` measures free-quota pressure (a Pro-conversion signal); `offline` and `error` measure feature availability.

## When it fires

Either a running generation fails, or a start attempt is blocked pre-flight:

- `limit_reached` — the generation stream throws `LimitReachedException` (coordinator marks the job `Failed(isLimitReached = true)` and the card locks).
- `error` — any other generation failure (coordinator `Failed(isLimitReached = false)`).
- `offline` — the pre-flight `isConnected()` check blocks the start and shows the offline snackbar.

## Trigger source

- `feature/day_study/.../domain/coordinator/DayStudyGenerationCoordinator.kt` — `start(...)`, `onFailure` branch (`limit_reached` / `error`)
- `feature/day_study/.../presentation/viewmodel/DayStudyViewModel.kt` — `startGenerationOrCachedOpen()`, the `!isConnected()` branch (`offline`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `limit_reached` | `limit_reached` \| `offline` \| `error` |
| `plan_type` | string | `chronological` | Reading plan of the day |
| `week_number` | int | `12` | 1-based week within the plan |
| `day_number` | int | `3` | 1-based day within the week |
| `is_pro` | boolean | `false` | Whether the user has the Pro entitlement |

## Notes

- `offline` never has a matching [day_study_generation_started](day_study_generation_started.md) (the start was blocked); `limit_reached`/`error` do.
- A separate concurrency guard (`canStartFreeGeneration` blocking when free in-flight generations ≥ `remainingFree`) also prevents a start with a "wait for generations" snackbar; it is not in the reason enum — decide during implementation whether to fold it into `limit_reached` or leave it untracked.
- `limit_reached` flips the card to `locked`, feeding [day_study_card_clicked](day_study_card_clicked.md) `card_mode=locked` and ultimately [paywall_viewed](paywall_viewed.md) `source=day_study`.
