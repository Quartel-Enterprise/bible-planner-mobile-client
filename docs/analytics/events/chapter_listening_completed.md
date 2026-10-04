# chapter_listening_completed

**Tier:** P1 | **Domain:** Listening

A chapter was read aloud to its last verse.

## When it fires

The voice finishes the last verse of the chapter (or of the verse range of a partial chapter in the reading of today).

## Trigger source

`core/chapter_listening/.../domain/controller/ChapterListeningControllerImpl.kt` — `onSegmentFinished`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `mode` | string | `chapter` | `chapter` when one chapter (and the ones after it) is playing, `day_reading` when the chapters of the plan day of today play in sequence |
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |

## Notes

- Followed by the next chapter's [chapter_listening_started](chapter_listening_started.md) (`cause=player`) when auto next or the reading of today goes on.
- Marking the finished chapter read from the offer that appears arrives as [chapter_read_toggled](chapter_read_toggled.md) with `source=listening_offer`.
