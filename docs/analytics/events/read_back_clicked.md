# read_back_clicked

**Tier:** P2 | **Domain:** Reading

Captures the user tapping the back arrow on the Read screen. Because this click also triggers navigation (which `destination_view` logs separately), this event isolates click-through intent from the resulting screen-view, so the two funnel signals aren't collapsed into one.

## When it fires

User taps the back arrow in the top bar of the Read screen.

## Trigger source

`feature/read/src/commonMain/kotlin/com/quare/bibleplanner/feature/read/presentation/model/ReadUiEvent.kt` — `ReadUiEvent.OnArrowBackClick`

## Parameters

None.

## Notes

- Related: [destination_view](destination_view.md) fires separately once the previous screen becomes visible again.
