# chapter_listening_started

**Tier:** P1 | **Domain:** Listening

A chapter started being read aloud by the device's text-to-speech voice. It is the top of the listening funnel and the denominator of [chapter_listening_completed](chapter_listening_completed.md).

## When it fires

The player opens a chapter and is about to speak it: a chapter or the reading of today started from the reader, the player moving on to the next chapter by itself or from its chapter buttons, or the reader opening another chapter while listening (the player follows it).

## Trigger source

`core/chapter_listening/.../domain/controller/ChapterListeningControllerImpl.kt` — `trackOpenedSegment`, once per chapter opened.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `mode` | string | `chapter` | `chapter` when one chapter (and the ones after it) is playing, `day_reading` when the chapters of the plan day of today play in sequence |
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |
| `cause` | string | `start` | Why this chapter opened: `start` (the person started listening), `player` (the player moved on: end of chapter, chapter buttons, notification), `reader` (the person opened another chapter in the reader while listening) |

## Notes

- Not sent when no voice is installed for the language of the Bible; [chapter_listening_voice_unavailable](chapter_listening_voice_unavailable.md) is sent instead.
- A free user must have unlocked the chapter first; see [chapter_listening_locked](chapter_listening_locked.md).
