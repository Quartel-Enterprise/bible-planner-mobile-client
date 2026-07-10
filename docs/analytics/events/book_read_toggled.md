# book_read_toggled

**Tier:** P1 | **Domain:** Reading

Captures the "mark all chapters" bulk action on the book details screen. Signals power usage (catching up on already-read books) and is distinct from organic chapter-by-chapter completion.

## When it fires

User taps the mark-all-chapters toggle on the book details screen.

## Trigger source

`feature/book_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/bookdetails/presentation/viewmodel/BookDetailsViewModel.kt` — `BookDetailsUiEvent.OnToggleAllChapters`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `genesis` | Book being toggled |
| `is_read` | boolean | `true` | New status: `true` marks all chapters read, `false` clears them |

## Notes

- `is_read` is the inverse of `areAllChaptersRead` in `BookDetailsUiState.Success` at the time of the tap.
- Do not fire one [chapter_read_toggled](chapter_read_toggled.md) per chapter for this bulk action.
- When `is_read=true` this always completes the book, so [book_completed](book_completed.md) fires with `source=toggle_all`.
