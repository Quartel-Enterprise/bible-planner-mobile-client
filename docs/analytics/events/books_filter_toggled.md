# books_filter_toggled

**Tier:** P2 | **Domain:** Books

Captures the user toggling one of the Books list filters (read, unread, favorites). Filter usage shows how users slice their reading progress and whether the favorites feature earns its place.

## When it fires

User taps a filter option in the Books filter menu, toggling it on or off.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnToggleFilter(filterType: BookFilterType)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `filter_type` | string | `favorites` | Toggled `BookFilterType` value: `only_read` \| `only_unread` \| `favorites` |
| `is_active` | boolean | `true` | Filter state after the toggle. The UiEvent carries only the type; the new state is read from the updated UiState |

## Notes

- Opening/closing the filter menu (`OnToggleFilterMenu`, `OnDismissFilterMenu`) is not tracked (see README "Explicitly not tracked").
- Related: [books_sort_changed](books_sort_changed.md), [books_search_used](books_search_used.md).
