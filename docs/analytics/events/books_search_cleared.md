# books_search_cleared

**Tier:** P2 | **Domain:** Books

Captures the user tapping the clear (X) button on the Books search field.

## When it fires

User taps the clear button on the search field on the Books tab.

## Trigger source

`feature/books/.../BooksViewModel.kt` — `BooksUiEvent.OnClearSearch`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Complements [books_search_used](books_search_used.md), which tracks entering a query.
