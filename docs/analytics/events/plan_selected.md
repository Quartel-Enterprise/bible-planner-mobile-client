# plan_selected

**Tier:** P1 | **Domain:** Reading

Captures switching the active reading plan (chronological vs books order). Shows which plan users prefer and how often they switch — input for defaulting and onboarding decisions.

## When it fires

User taps a plan option in the plan selector on the reading plan screen.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnPlanClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `books` | Plan that was selected |

## Notes

- The handler calls `setSelectedReadingPlan` unconditionally, including taps on the already-selected plan — skip logging when `event.type == uiState.selectedReadingPlan` so the event means an actual switch.
- Both plans share the same read data; switching does not change progress, only ordering.
- The selected plan is also proposed as the `selected_plan_type` user property (see README).
