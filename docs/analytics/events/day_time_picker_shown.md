# day_time_picker_shown

**Tier:** P2 | **Domain:** Reading

Captures the user switching from the date view to the time view within the open read-date editor on the Day screen.

## When it fires

Within the already-open date/time picker (opened via [day_edit_date_clicked](day_edit_date_clicked.md)), the user switches to the time-picking step.

## Trigger source

`feature/day/.../DayViewModel.kt` — `DayUiEvent.OnShowTimePicker`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- An intra-flow step, not a fresh entry point — it can only fire after [day_edit_date_clicked](day_edit_date_clicked.md).
