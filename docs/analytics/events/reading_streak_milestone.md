# reading_streak_milestone

**Tier:** P2 | **Domain:** Reading

Derived milestone: the user's consecutive-day reading streak hit a notable length. Habit strength is the best retention predictor this app has, so streak milestones segment engaged users.

## When it fires

When the streak reaches 7, 30, 100 or 365 consecutive days — at most once per threshold per streak.

## Trigger source

Derived from `PlanStatus.streakDays`, computed by `feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/domain/usecase/impl/ResolvePlanStatusUseCase.kt` (`calculateStreak`) and exposed on `ReadingPlanUiState.Loaded.planStatus`. Compare the new streak value against the previous one whenever plan status is recomputed.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `streak_days` | int | `30` | Threshold reached: `7` \| `30` \| `100` \| `365` |

## Notes

- Persist the highest threshold fired for the *current* streak; reset that marker when the streak breaks (streak value drops), so a new streak can earn the milestones again.
- `PlanStatus` is only resolved while the plan screen state is built; if milestones must fire even for users who mark days read elsewhere, move the streak derivation to the domain layer alongside `UpdateDayReadStatusUseCase`.
- Related: [day_read_toggled](day_read_toggled.md) is the action that extends a streak.
