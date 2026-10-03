# rewarded_ad_earned

**Tier:** P1 | **Domain:** Monetization

Captures a rewarded video watched to the reward. The app then asks the server for the study with `reward: true`, so this is the client half of every rewarded unlock; compare it with the `source = 'rewarded'` unlock rows in the database and with AdMob's rewards to spot forged rewards.

## When it fires

The rewarded ad is closed after AdMob granted the reward. The sheet closes and the surface that opened it starts the generation as a rewarded unlock.

## Trigger source

`feature/study_unlock/.../presentation/viewmodel/StudyUnlockViewModel.kt` — `StudyUnlockUiEvent.OnWatchVideoClick` → `RewardedAdResult.Earned`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `chapter_study` | Which locked surface offered the unlock: `StudyUnlockSurface` in `core/model/.../route/`, lowercased (`chapter_study` \| `day_study` \| `day_reading_complete`) |

## Notes

- The generation that follows carries `is_rewarded=true` on [day_study_generation_started](day_study_generation_started.md) / [chapter_study_generation_started](chapter_study_generation_started.md) and their completion and failure events.
- A failed generation after the reward can be retried for free: the coordinator remembers the unserved reward and the next attempt sends `reward: true` again without another video.
