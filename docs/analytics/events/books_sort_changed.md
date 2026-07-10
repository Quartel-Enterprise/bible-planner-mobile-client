# books_sort_changed

**Tier:** P2 | **Domain:** Books

Captures the user changing the sort order of the Books list. Tells us whether anyone sorts away from the default, informing whether the sort menu is worth its UI cost.

## When it fires

User selects a sort order in the Books sort menu.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnSortOrderSelect(sortOrder: BookSortOrder)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `sort_order` | string | `alphabetical_ascending` | Selected `BookSortOrder` value: `alphabetical_ascending` \| `alphabetical_descending` |

## Notes

- Opening/closing the sort menu (`OnToggleSortMenu`, `OnDismissSortMenu`) is not tracked (see README "Explicitly not tracked").
- Related: [books_filter_toggled](books_filter_toggled.md), [books_layout_changed](books_layout_changed.md).
