# chapter_study_entry_clicked

**Tier:** P1 | **Domain:** ChapterStudy

Captures the reader asking for the AI study of the chapter they are reading. It is the top of the chapter-study funnel: what follows is a study screen ([screen_view](screen_view.md) `chapter_study`), the login warning (`reason=chapter_study`) or the Pro teaser (`reason=chapter_study_limit`), so the split between those three shows how much of the demand each gate turns away.

## When it fires

The reader taps the "Study" pill in the reader's top bar, or the "Study this chapter" card that closes each chapter.

## Trigger source

`feature/read/.../presentation/model/ReadUiEvent.kt` — `OnChapterStudyClick`, tracked automatically. `ReadViewModel.openChapterStudy` then resolves where the tap leads through `GetChapterStudyAccess`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |
| `source` | string | `top_bar` | `top_bar` \| `chapter_end` (`ChapterStudyEntrySource`, lowercased) |

## Notes

- In vertical reading the pill names the chapter at the top of the screen, and each chapter has its own card, so `chapter_number` is the chapter the reader meant, not the one the screen was opened on.
- A second tap while the first one is still being resolved is ignored by the ViewModel but still logged.
