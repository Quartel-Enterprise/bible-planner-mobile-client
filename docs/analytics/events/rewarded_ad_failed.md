# rewarded_ad_failed

**Tier:** P1 | **Domain:** Monetization

Captures a rewarded video that could not be loaded or shown. A high `no_fill` share for a market means AdMob has no demand there, and the sheet keeps showing "No video available right now".

## When it fires

- While the unlock sheet is open, the preload ends without an ad (`no_fill` or `load_error`).
- After [rewarded_ad_started](rewarded_ad_started.md), the ad fails to show (`show_error`).

## Trigger source

`feature/study_unlock/.../presentation/viewmodel/StudyUnlockViewModel.kt` — the ad availability observer (load failures) and `StudyUnlockUiEvent.OnWatchVideoClick` → `RewardedAdResult.Failed` (show failures).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `chapter_study` | Which locked surface offered the unlock: `StudyUnlockSurface` in `core/model/.../route/`, lowercased (`chapter_study` \| `day_study` \| `day_reading_complete`) |
| `reason` | string | `no_fill` | `RewardedAdFailureReason` in `core/provider/ads`, lowercased: `no_fill` (AdMob had no ad), `load_error` (the request failed, or consent does not allow ads) or `show_error` (a loaded ad could not be presented) |
