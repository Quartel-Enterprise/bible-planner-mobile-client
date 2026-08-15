# day_back_clicked

**Tier:** P2 | **Domain:** Reading

Captures the user tapping the back control on the Day screen. Because this click also triggers navigation (which `screen_view` logs separately), this event isolates click-through intent from the resulting screen-view.

## When it fires

User taps the back control in the top bar of the Day screen.

## Trigger source

`feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/model/DayUiEvent.kt` — `DayUiEvent.OnBackClick`

## Parameters

None.

## Notes

- Related: [screen_view](screen_view.md) fires separately once the previous screen becomes visible again.
