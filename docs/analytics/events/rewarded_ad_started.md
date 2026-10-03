# rewarded_ad_started

**Tier:** P1 | **Domain:** Monetization

Captures the user asking to watch a rewarded video to unlock one more study. Its outcomes are [rewarded_ad_earned](rewarded_ad_earned.md), [rewarded_ad_dismissed](rewarded_ad_dismissed.md) and [rewarded_ad_failed](rewarded_ad_failed.md) with `reason=show_error`.

## When it fires

The user taps "Watch a short video" on the unlock sheet while a preloaded ad is ready. The button is disabled while the ad loads or when none is available, so the tap never fires without an ad to show.

## Trigger source

`feature/study_unlock/.../presentation/model/StudyUnlockUiEvent.kt` — `StudyUnlockUiEvent.OnWatchVideoClick`, tracked from `StudyUnlockViewModel.handleEvent`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `chapter_study` | Which locked surface offered the unlock: `StudyUnlockSurface` in `core/model/.../route/`, lowercased (`chapter_study` \| `day_study` \| `day_reading_complete`) |

## Notes

- AdMob's own `ad_impression` (with its revenue value) reaches GA4 through the AdMob ↔ Firebase link; this event is the app's side of the same moment.
