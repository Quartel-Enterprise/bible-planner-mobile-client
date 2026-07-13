# books_filter_menu_opened

**Tier:** P2 | **Domain:** Books

Captures the user opening the filter menu on the Books tab. Menu open rate contextualizes how often filters are discovered before [books_filter_toggled](books_filter_toggled.md) fires.

## When it fires

User taps the filter icon on the Books tab, opening the filter dropdown menu.

## Trigger source

`feature/books/.../BooksViewModel.kt` — `BooksUiEvent.OnToggleFilterMenu`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Closing the menu (via tap-outside or back) is tracked separately: [books_filter_menu_dismissed](books_filter_menu_dismissed.md).
