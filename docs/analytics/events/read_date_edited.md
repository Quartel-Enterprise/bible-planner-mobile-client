# read_date_edited

**Tier:** P2 | **Domain:** Reading

Captures retroactively editing the date/time a day was read. Indicates users who backfill progress (read offline or in another Bible) rather than reading in-app.

## When it fires

User confirms the time picker after picking a date for a day's read timestamp on the Day screen (the date picker flows into the time picker; confirmation of the time commits the edit).

## Trigger source

`feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/viewmodel/DayViewModel.kt` — `DayUiEvent.OnEditReadDate`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Active reading plan |
| `week_number` | int | `12` | 1-based week of the edited day |
| `day_number` | int | `3` | 1-based day within the week |

## Notes

- Intermediate picker steps (`OnEditDateClick`, `OnDateSelected`, `OnShowTimePicker`) and dismissals (`OnDismissPicker`) are not tracked — only the committed edit.
- `OnEditReadDate` is a no-op when no date was selected in the picker state; do not log that early-return path.
- Related: [day_read_toggled](day_read_toggled.md) marks the day read in the first place; this event only adjusts *when*.
