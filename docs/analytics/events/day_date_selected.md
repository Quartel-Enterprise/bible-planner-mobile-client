# day_date_selected

**Tier:** P2 | **Domain:** Reading

Captures the user picking a date value inside the open read-date editor on the Day screen, before confirming.

## When it fires

Within the open date/time picker, the user taps a day in the calendar. May fire multiple times in a single edit session if the user changes their mind before confirming.

## Trigger source

`feature/day/.../DayViewModel.kt` — `DayUiEvent.OnDateSelected`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None. The picked timestamp itself is not logged — only that a selection happened.

## Notes

- An intermediate, possibly-repeated step; the final committed value is tracked by [read_date_edited](read_date_edited.md).
