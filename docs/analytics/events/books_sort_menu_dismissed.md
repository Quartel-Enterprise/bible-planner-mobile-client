# books_sort_menu_dismissed

**Tier:** P2 | **Domain:** Books

Captures the sort menu on the Books tab closing, whether by tapping a sort option, tapping outside, or pressing back. Compared against [books_sort_menu_opened](books_sort_menu_opened.md), it shows how often the menu is opened without a sort change.

## When it fires

The sort dropdown's `onDismissRequest` fires — this is Compose's generic dismiss callback, so it does not distinguish the exact cause (tap-outside, back press, or a sort selection that also closes the menu).

## Trigger source

`feature/books/.../BooksViewModel.kt` — `BooksUiEvent.OnDismissSortMenu`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Because this fires from a generic dismiss callback, it is not a precise "user declined sorting" signal — treat volume trends directionally, not as a strict funnel step.
