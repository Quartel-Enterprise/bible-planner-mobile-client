# rewarded_ad_dismissed

**Tier:** P2 | **Domain:** Monetization

Captures a rewarded video closed before the reward. The study stays locked and the sheet stays open with a new ad loading.

## When it fires

The rewarded ad is closed without AdMob granting the reward.

## Trigger source

`feature/study_unlock/.../presentation/viewmodel/StudyUnlockViewModel.kt` — `StudyUnlockUiEvent.OnWatchVideoClick` → `RewardedAdResult.Dismissed`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `chapter_study` | Which locked surface offered the unlock: `StudyUnlockSurface` in `core/model/.../route/`, lowercased (`chapter_study` \| `day_study` \| `day_reading_complete`) |
