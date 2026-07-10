# book_completed

**Tier:** P1 | **Domain:** Reading

Derived milestone: the user finished a whole book of the Bible. A strong retention signal and a natural celebration/notification hook.

## When it fires

When a read toggle causes all chapters of a book to become read — i.e. the book transitions from "not all chapters read" to "all chapters read". Not fired on every subsequent read of an already-complete book.

## Trigger source

Derived, not a direct UiEvent. Completion is detectable wherever chapter read state is written:

- `feature/book_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/bookdetails/presentation/viewmodel/BookDetailsViewModel.kt` — `BookDetailsUiEvent.OnToggleAllChapters` (`source=toggle_all`)
- any surface firing [chapter_read_toggled](chapter_read_toggled.md) or [day_read_toggled](day_read_toggled.md) whose write marks the book's last unread chapter (`source=last_chapter`)

The cleanest implementation point is the domain layer (e.g. `UpdateBookReadStatusUseCase` / `ToggleWholeChapterReadStatusUseCase` in `core/books`), comparing chapter state before and after the write.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `genesis` | Book that was completed |
| `source` | string | `toggle_all` \| `last_chapter` | Bulk toggle vs organically finishing the last unread chapter |

## Notes

- Fires only on the unread → fully-read transition. Un-reading a chapter and re-reading it later fires the event again — that is intentional (it is a state transition, not a once-per-lifetime milestone).
- Emitted in addition to the triggering [chapter_read_toggled](chapter_read_toggled.md) / [book_read_toggled](book_read_toggled.md), never instead of it.
- Do not fire when a progress reset ([progress_reset_confirmed](progress_reset_confirmed.md)) rewrites state.
