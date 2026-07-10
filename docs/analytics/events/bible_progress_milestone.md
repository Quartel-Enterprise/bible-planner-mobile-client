# bible_progress_milestone

**Tier:** P2 | **Domain:** Reading

Derived milestone: overall Bible reading progress crossed a quarter threshold. Gives a coarse, plan-independent progression funnel (how many users ever reach 25%, 50%, ...).

## When it fires

When the verse-based overall progress crosses 25, 50, 75 or 100 percent — at most once per threshold crossing.

## Trigger source

Derived from `core/books/src/commonMain/kotlin/com/quare/bibleplanner/core/books/domain/usecase/CalculateBibleProgressUseCase.kt`, which emits a `Flow<Float>` in the 0–100 range (read verses / total verses). Observe the flow (it is already collected in `feature/reading_plan/.../ReadingPlanViewModel.kt`) and compare each emission against the previous value.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `percent` | int | `50` | Threshold crossed: `25` \| `50` \| `75` \| `100` |

## Notes

- Fire once per upward crossing: persist the highest threshold already reported and only log thresholds above it. A single bulk action (e.g. [book_read_toggled](book_read_toggled.md)) can cross several thresholds at once — fire one event per threshold crossed.
- Un-reading below a threshold and re-crossing it must not re-fire; only reset the persisted high-water mark on [progress_reset_confirmed](progress_reset_confirmed.md).
- Progress is verse-based, not chapter-based, so `percent=100` coincides with all chapters read; it usually accompanies [plan_completed](plan_completed.md).
