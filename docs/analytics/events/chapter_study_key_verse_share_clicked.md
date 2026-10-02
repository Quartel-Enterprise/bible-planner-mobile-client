# chapter_study_key_verse_share_clicked

**Tier:** P2 | **Domain:** ChapterStudy

Captures the reader choosing to share the chapter's key verse from the study.

## When it fires

The reader taps "Share" on the key-verse card. The verse share sheet opens for those verses.

## Trigger source

`feature/chapter_study/.../presentation/viewmodel/ChapterStudyViewModel.kt` — `ChapterStudyUiEvent.OnShareKeyVerseClick` → `onShareKeyVerseClick()`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |

## Notes

- This is the entry into the share flow, not a completed share: what the reader does in the sheet is covered by the verse-sharing events ([verse_shared](verse_shared.md)).
