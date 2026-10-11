# chapter_listening_free_unlocked

**Tier:** P1 | **Domain:** Listening

A free user got the day's free chapter without watching a video, because no rewarded video could be offered.

## When it fires

A free user asks to listen to a locked chapter while rewarded videos are off (Remote Config `rewarded_ads_enabled`, or no video left today) and the free chapters of the day (Remote Config `listening_free_daily_limit`, default 1; 0 turns them off) aren't used up yet. The chapter is unlocked for today and starts playing right away.

It also fires when the video is on but fails to load, and the person takes the free chapter from the unlock sheet instead (right after [unlock_without_video_clicked](unlock_without_video_clicked.md)).

## Trigger source

`feature/read/.../presentation/listening/ChapterListeningGate.kt` — `trackFreeUnlock`, from `unlockForFree` or when the unlock sheet reports `StudyUnlockResultModel.UNLOCKED_WITHOUT_VIDEO`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `gen` | Book of the chapter, lowercased `BookId` |
| `chapter_number` | int | `3` | Chapter number |

## Notes

- Free chapters count against the same daily unlocks as the rewarded ones.
- The entry, unlock-next and player unlock clicks declare it in their `Track.Manual` names, since their branch goes through the gate.
