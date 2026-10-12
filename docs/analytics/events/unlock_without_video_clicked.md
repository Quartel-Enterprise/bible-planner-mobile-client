# unlock_without_video_clicked

**Tier:** P1 | **Domain:** Monetization

The user took the free unlock the sheet offers when no rewarded video can be shown. Against [rewarded_ad_failed](rewarded_ad_failed.md) with `no_fill` / `load_error` it shows how often an unserved video still ends in listening.

## When it fires

The user taps "Listen free today" on the unlock sheet. The button replaces the video one only when the video is unavailable and the caller allows a free unlock (today, only chapter listening, within Remote Config `listening_free_daily_limit`).

## Trigger source

`feature/study_unlock/.../presentation/model/StudyUnlockUiEvent.kt` — `StudyUnlockUiEvent.OnUnlockWithoutVideoClick`, tracked in `StudyUnlockViewModel` only when the free unlock goes through.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `chapter_listening` | Which locked surface offered the unlock: `StudyUnlockSurface` in `core/model/.../route/`, lowercased |

## Notes

- Closes the sheet and completes the unlock like an earned reward, so the chapter starts playing.
- When rewarded videos are switched off, the free chapter is granted without the sheet; see [chapter_listening_free_unlocked](chapter_listening_free_unlocked.md).
