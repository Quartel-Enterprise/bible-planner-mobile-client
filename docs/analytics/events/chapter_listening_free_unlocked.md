# chapter_listening_free_unlocked

**Tier:** P1 | **Domain:** Listening

A free user got the day's free chapter without watching a video, because no rewarded video could be offered.

## When it fires

A free user asks to listen to a locked chapter while rewarded videos are off (Remote Config `rewarded_ads_enabled`, or no video left today) and the free chapters of the day (Remote Config `listening_free_daily_limit`, default 1; 0 turns them off) aren't used up yet. The chapter is unlocked for today and starts playing right away.

## Trigger source

`feature/read/.../presentation/listening/ChapterListeningGate.kt` — `unlockForFree`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |

## Notes

- Free chapters count against the same daily unlocks as the rewarded ones.
- When the video is on but fails to load, the unlock sheet offers the free chapter instead; see [unlock_without_video_clicked](unlock_without_video_clicked.md).
