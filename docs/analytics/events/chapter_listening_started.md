# chapter_listening_started

**Tier:** P1 | **Domain:** Listening

A chapter started playing its narrated audio. It is the top of the listening funnel and the denominator of [chapter_listening_completed](chapter_listening_completed.md).

## When it fires

A chapter's audio first plays after it was opened: a chapter or the reading of today started from the reader, the player moving on to the next chapter by itself or from its chapter buttons, or the reader opening another chapter while listening (the player follows it). A chapter paused while it loaded, or moved to while paused, counts when it is first played, once.

## Trigger source

`core/chapter_listening/.../domain/controller/ListeningPlayback.kt` — `trackStartOnce`, from `playFrom`, once per opened chapter that plays.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `mode` | string | `chapter` | `chapter` when one chapter (and the ones after it) is playing, `day_reading` when the chapters of the plan day of today play in sequence |
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |
| `cause` | string | `start` | Why this chapter opened: `start` (the person started listening), `player` (the player moved on: end of chapter, chapter buttons, notification), `reader` (the person opened another chapter in the reader while listening) |

## Notes

- Not sent when the chapter's audio can't be loaded; [chapter_listening_audio_unavailable](chapter_listening_audio_unavailable.md) is sent instead.
- A free user must have unlocked the chapter first; see [chapter_listening_locked](chapter_listening_locked.md).
