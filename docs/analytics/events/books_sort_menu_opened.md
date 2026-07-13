# books_sort_menu_opened

**Tier:** P2 | **Domain:** Books

Captures the user opening the sort menu on the Books tab. Menu open rate contextualizes how often sorting is discovered before [books_sort_changed](books_sort_changed.md) fires.

## When it fires

User taps the sort icon on the Books tab, opening the sort dropdown menu.

## Trigger source

`feature/books/.../BooksViewModel.kt` — `BooksUiEvent.OnToggleSortMenu`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Closing the menu (via tap-outside or back) is tracked separately: [books_sort_menu_dismissed](books_sort_menu_dismissed.md).
