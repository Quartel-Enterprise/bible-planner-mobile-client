# chapter_listening_audio_unavailable

**Tier:** P2 | **Domain:** Listening

Listening was asked for but the chapter's audio could not be loaded, so the player shows why instead of playing.

## When it fires

The player opens a chapter whose audio is neither cached on the device nor returned by the `get-chapter-audio` function.

## Trigger source

`core/chapter_listening/.../domain/controller/ChapterListeningControllerImpl.kt` — `trackOpenedSegment`, once per chapter opened.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `mode` | string | `chapter` | `chapter` or `day_reading`, as in [chapter_listening_started](chapter_listening_started.md) |
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |
| `reason` | string | `no_connection` | `no_connection` (offline and not cached), `not_signed_in` (the session expired), `version_not_enabled` (the version is outside `listening_cloud_versions`), `budget_reached` (the chapter has no audio yet and this month's generation budget is spent), `unavailable` (the generation or the download failed) |

## Notes

- `budget_reached` counts the people the monthly cap (`chapter_audio_monthly_budget_usd`) turned away; a steady stream of it means the cap is too low.
