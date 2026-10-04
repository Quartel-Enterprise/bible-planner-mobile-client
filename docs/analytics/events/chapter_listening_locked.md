# chapter_listening_locked

**Tier:** P1 | **Domain:** Listening

A free user reached a chapter they have not unlocked for listening.

## When it fires

The player tries to move to another chapter (end of chapter with auto next or the reading of today, chapter buttons, or the reader opening another chapter) and the chapter is neither Pro nor unlocked today.

## Trigger source

`core/chapter_listening/.../domain/controller/ChapterListeningControllerImpl.kt` — `changeSegment`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |

## Notes

- The player stops and offers the unlock ([chapter_listening_unlock_clicked](chapter_listening_unlock_clicked.md)).
