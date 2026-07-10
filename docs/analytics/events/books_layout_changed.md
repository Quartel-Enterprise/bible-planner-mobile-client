# books_layout_changed

**Tier:** P2 | **Domain:** Books

Captures the user switching the Books tab between list and grid layout. Layout preference distribution decides which presentation deserves design attention and which default to ship.

## When it fires

User taps the layout switcher on the Books tab.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnLayoutFormatSelect(layoutFormat: BookLayoutFormat)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `layout` | string | `grid` | Selected `BookLayoutFormat` value: `list` \| `grid` |

## Notes

- Related: [books_sort_changed](books_sort_changed.md), [testament_switched](testament_switched.md).
