# synopsis_toggled

**Tier:** P2 | **Domain:** Books

Captures the user expanding or collapsing the synopsis section on a book-details screen. Expansion rate tells us whether the synopses are read at all and for which books, guiding content investment.

## When it fires

User taps the synopsis header on the book-details screen to expand or collapse it.

## Trigger source

`feature/book_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/bookdetails/presentation/viewmodel/BookDetailsViewModel.kt` — `BookDetailsUiEvent.OnToggleSynopsisExpanded`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `genesis` | Standard param; the book whose synopsis was toggled |
| `is_expanded` | boolean | `true` | Synopsis state after the toggle |

## Notes

- `OnToggleSynopsisExpanded` is a parameterless `data object`: both params come from ViewModel state (`book_id` from the route, `is_expanded` from the UiState after the toggle), not from the UiEvent.
- Related: [destination_view](destination_view.md) (`book_details` with `book_id`), [book_read_toggled](book_read_toggled.md).
