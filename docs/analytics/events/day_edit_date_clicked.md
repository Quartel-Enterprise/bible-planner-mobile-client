# day_edit_date_clicked

**Tier:** P2 | **Domain:** Reading

Captures the user tapping the edit-date affordance on the Day screen, opening the date/time picker. This is the entry point into the [read_date_edited](read_date_edited.md) funnel.

## When it fires

User taps the edit-date control on the Day screen.

## Trigger source

`feature/day/.../DayViewModel.kt` — `DayUiEvent.OnEditDateClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Only fires when the picker is opened; the final committed change is tracked separately by [read_date_edited](read_date_edited.md).
