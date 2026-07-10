# books_search_used

**Tier:** P2 | **Domain:** Books

Captures the user searching for a book by name on the Books tab. Search usage reveals whether the list/grid browsing is sufficient or users prefer direct lookup, and query length hints at how much they type before finding a match.

## When it fires

User types in the Books search field. `OnSearchQueryChange` fires on every keystroke, so instrumentation must debounce: log once per typing burst (or once per screen session) with a non-empty query.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnSearchQueryChange(query: String)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `query_length` | int | `4` | Character count of the debounced query. The query text itself is never logged |

## Notes

- The raw query is deliberately not sent — book names are not sensitive, but `query_length` answers the analytical question without free-text params.
- `OnClearSearch` is not tracked (UI bookkeeping, see README "Explicitly not tracked").
- Related: [books_filter_toggled](books_filter_toggled.md), [testament_switched](testament_switched.md).
