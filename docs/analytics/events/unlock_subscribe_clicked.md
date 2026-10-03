# unlock_subscribe_clicked

**Tier:** P1 | **Domain:** Monetization

Captures the user choosing Pro on the unlock sheet — the primary action, kept above the video so the rewarded option does not cannibalize the paywall. Against [rewarded_ad_started](rewarded_ad_started.md) it measures how the sheet splits the hottest Pro leads.

## When it fires

The user taps "Subscribe to Pro" on the unlock sheet.

## Trigger source

`feature/study_unlock/.../presentation/model/StudyUnlockUiEvent.kt` — `StudyUnlockUiEvent.OnSubscribeClick`, tracked from `StudyUnlockViewModel.handleEvent`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `chapter_study` | Which locked surface offered the unlock: `StudyUnlockSurface` in `core/model/.../route/`, lowercased (`chapter_study` \| `day_study` \| `day_reading_complete`) |

## Notes

- Replaces the sheet with the paywall, so the next impression is [paywall_viewed](paywall_viewed.md) with the surface's `source` (`chapter_study`, `day_study` or `day_study_detail`).
