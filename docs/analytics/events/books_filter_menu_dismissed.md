# books_filter_menu_dismissed

**Tier:** P2 | **Domain:** Books

Captures the filter menu on the Books tab closing, whether by tapping a filter, tapping outside, or pressing back. Compared against [books_filter_menu_opened](books_filter_menu_opened.md), it shows how often the menu is opened without a filter change.

## When it fires

The filter dropdown's `onDismissRequest` fires — this is Compose's generic dismiss callback, so it does not distinguish the exact cause (tap-outside, back press, or a filter selection that also closes the menu).

## Trigger source

`feature/books/.../BooksViewModel.kt` — `BooksUiEvent.OnDismissFilterMenu`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Because this fires from a generic dismiss callback, it is not a precise "user declined filtering" signal — treat volume trends directionally, not as a strict funnel step.
