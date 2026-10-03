# unlock_sheet_viewed

**Tier:** P1 | **Domain:** Monetization

Captures a free user with no free AI study left reaching the unlock sheet, where they choose between subscribing and watching a rewarded video for one more study. It is the top of the rewarded-unlock funnel, and the denominator for both its exits: [unlock_subscribe_clicked](unlock_subscribe_clicked.md) and [rewarded_ad_started](rewarded_ad_started.md).

## When it fires

The unlock sheet opens. A locked surface opens it from its `Unlock` call to action only when the platform supports ads, the `rewarded_ads_enabled` Remote Config flag is on and the server reports rewarded unlocks left today; otherwise the surface keeps its `Subscribe` call to action and never opens the sheet.

## Trigger source

`feature/study_unlock/.../presentation/viewmodel/StudyUnlockViewModel.kt` — tracked from `init`, once per sheet.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `chapter_study` | Which locked surface offered the unlock: `StudyUnlockSurface` in `core/model/.../route/`, lowercased (`chapter_study` \| `day_study` \| `day_reading_complete`) |
| `rewarded_remaining_today` | int | `2` | Rewarded unlocks the server still allows in the rolling 24 h window, shared by the day and the chapter study (`rewarded_remaining_today` from the status endpoints) |

## Notes

- The matching impression is [screen_view](screen_view.md) with `screen_name=study_unlock`.
- `rewarded_ads_enabled` is on for everyone (no A/B experiment), so watch this event's exits against the paywall conversion from the locked surfaces before and after the release, not just the ad revenue.
