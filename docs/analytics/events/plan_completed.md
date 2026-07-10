# plan_completed

**Tier:** P1 | **Domain:** Reading

Derived milestone: the user finished the entire reading plan — every day of every week read. The terminal conversion of the core product funnel; expected to be rare and worth celebrating.

## When it fires

When a read toggle causes the last unread day of the selected plan to become read — the plan transitions from "has unread days" to "all days read".

## Trigger source

Derived, not a direct UiEvent. Detect at the domain layer where day read status is written (`UpdateDayReadStatusUseCase` in `core/plan`), by checking whether all weeks returned by `GetPlansByWeekUseCase` for that plan are fully read after the write. Reached from any surface that fires [day_read_toggled](day_read_toggled.md) or [chapter_read_toggled](chapter_read_toggled.md).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Plan that was completed |

## Notes

- Both plans (`chronological`, `books`) share the same underlying read data, so completing one typically completes the other; fire once per `plan_type` whose state transitioned.
- Always accompanied by the final [week_completed](week_completed.md) and usually [bible_progress_milestone](bible_progress_milestone.md) with `percent=100`.
- Fires again only if progress is reset ([progress_reset_confirmed](progress_reset_confirmed.md)) and the plan is completed a second time.
