# week_completed

**Tier:** P1 | **Domain:** Reading

Derived milestone: every day of a plan week became read. Measures steady cadence through the reading plan and is the natural unit for retention cohorts (weeks completed per user).

## When it fires

When a day read toggle causes all days of a week to become read — the week transitions from "has unread days" to "all days read".

## Trigger source

Derived, not a direct UiEvent. The UI already detects this transition for its auto-advance behavior:

- `feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `focusCurrentWeekIfJustCompleted(...)`, reached from `ReadingPlanUiEvent.OnDayReadClick`

That hook only covers toggles made from the plan list. For full coverage (Day screen toggles, chapter toggles that complete the last day), derive at the domain layer where day read status is written (`UpdateDayReadStatusUseCase` in `core/plan`), comparing week state before and after — the plan's stated trigger ("ReadingPlan auto-advance") alone would under-count.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Plan the week belongs to |
| `week_number` | int | `12` | 1-based completed week |

## Notes

- Fires only on the transition; toggling a day off and on again within an already-complete week re-fires, which is acceptable but should be rare.
- Completing the final week also fires [plan_completed](plan_completed.md).
- Do not fire during progress reset ([progress_reset_confirmed](progress_reset_confirmed.md)) or plan start date recalculation.
