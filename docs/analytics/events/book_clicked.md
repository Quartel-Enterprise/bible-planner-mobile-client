# book_clicked

**Tier:** P2 | **Domain:** Reading

Captures the user tapping a book in the Books list to open its book-details screen. This click also triggers navigation (which `destination_view` logs separately), so this event isolates click-through intent — which books get tapped, from which list state — from the resulting screen-view.

## When it fires

User taps a book row/card on the Books screen.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/model/BooksUiEvent.kt` — `BooksUiEvent.OnBookClick(book: BookPresentationModel)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `genesis` | Standard param; `book.id.name.lowercase()` from `BookPresentationModel` |

## Notes

- Related: [destination_view](destination_view.md) (`book_details` with `book_id`) fires separately once the destination becomes visible.
