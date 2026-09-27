# annotation_chapter_opened

**Tier:** P1 | **Domain:** Verse annotations

Going from a saved annotation back into the reader — the main way this screen feeds reading.

## When it fires

The user taps an annotation row, or picks Open in chapter from its menu.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnItemClick` and `AnnotationsUiEvent.OnOpenInChapterClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | row | `row` when the row itself was tapped, `menu` when the menu option was used |
| `book_id` | string | jhn | Lowercased book id of the passage |
| `chapter_number` | integer | 3 | Chapter of the passage |

## Notes

- Followed by the `screen_view` of `read`.
