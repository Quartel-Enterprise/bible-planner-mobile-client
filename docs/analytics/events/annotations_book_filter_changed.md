# annotations_book_filter_changed

**Tier:** P2 | **Domain:** Verse annotations

Narrowing the list to the annotations of one book.

## When it fires

The user picks a book (or "All books") in the book menu on the phone layout or in the Book section of the wide filter panel. Picking the selected book again clears the book filter.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnBookSelected`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | jhn | Lowercased id of the book applied after the tap, or `all` when the filter was cleared |

## Notes

- Tracked manually because the applied book depends on the one selected before the tap.
