# chapter_listening_interrupted

**Tier:** P2 | **Domain:** Listening

Listening was interrupted by another app taking the audio, such as a phone call.

## When it fires

The system takes the audio focus away for a while (Android) or begins an audio session interruption (iOS) while a chapter is being read.

## Trigger source

`core/chapter_listening/.../domain/controller/ChapterListeningControllerImpl.kt` — `interrupt`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `mode` | string | `chapter` | `chapter` when one chapter (and the ones after it) is playing, `day_reading` when the chapters of the plan day of today play in sequence |
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |

## Notes

- Listening does not resume by itself; the person resumes it from the mini player ([chapter_listening_control_clicked](chapter_listening_control_clicked.md) with `control=resume`).
