# day_date_picker_dismissed

**Tier:** P2 | **Domain:** Reading

Captures the read-date picker on the Day screen closing without a confirmed change.

## When it fires

The date/time picker dialog's dismiss callback fires (tap-outside, back press, or an explicit close) without reaching [read_date_edited](read_date_edited.md).

## Trigger source

`feature/day/.../DayViewModel.kt` — `DayUiEvent.OnDismissPicker`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Generic dismiss callback — does not distinguish the exact cause.
