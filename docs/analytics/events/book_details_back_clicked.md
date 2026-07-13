# book_details_back_clicked

**Tier:** P2 | **Domain:** Reading

Captures the user tapping the back control on the book-details screen. Because this click also triggers navigation (which `destination_view` logs separately), this event isolates click-through intent from the resulting screen-view.

## When it fires

User taps the back control in the top bar of the book-details screen.

## Trigger source

`feature/book_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/bookdetails/presentation/model/BookDetailsUiModels.kt` — `BookDetailsUiEvent.OnBackClick`

## Parameters

None.

## Notes

- Related: [destination_view](destination_view.md) fires separately once the previous screen becomes visible again.
