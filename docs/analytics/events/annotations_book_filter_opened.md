# annotations_book_filter_opened

**Tier:** P2 | **Domain:** Verse annotations

The book filter menu was opened on the phone layout.

## When it fires

The user taps the Book chip in the filter bar.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnBookFilterClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- The wide layout lists the books inline, so it never fires this event.
