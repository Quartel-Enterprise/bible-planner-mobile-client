# day_card_clicked

**Tier:** P2 | **Domain:** Reading

Captures tapping a day card on the reading plan screen to open that day. Click-through on the day card is a distinct funnel signal from the resulting Day screen impression, so it is tracked separately from `destination_view`.

## When it fires

User taps a day card in the reading plan list.

## Trigger source

`feature/reading_plan/src/commonMain/kotlin/com/quare/bibleplanner/feature/readingplan/presentation/viewmodel/ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnDayClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `week_number` | int | `12` | 1-based week of the tapped day |
| `day_number` | int | `3` | 1-based day within the week |

## Notes

- The subsequent Day screen impression is separately captured by [destination_view](destination_view.md); this event only covers the card tap itself.
- Distinct from [day_read_toggled](day_read_toggled.md), which marks the day as read via the checkbox, not the card tap.
