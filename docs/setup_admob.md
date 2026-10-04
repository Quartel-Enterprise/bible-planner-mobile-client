# AdMob Setup Guide

A free user with no free AI study left can watch a rewarded video (AdMob) to unlock one more study,
on the chapter study, the day study and the day reading complete card. Subscribing stays the
primary action. Android and iOS only: AdMob has no JVM SDK, so desktop never offers the video.

## How it fits together

| Piece | Where | What it does |
|---|---|---|
| `:core:provider:ads` | `RewardedAdService`, `AdsConsentService` | Loads and shows the rewarded ad and gathers consent (Google UMP). Android uses the Google Mobile Ads SDK, iOS a Swift `RewardedAdDataSource` / `AdsConsentDataSource` handed to Koin by `initializeKoinForIos`, desktop an unsupported data source. |
| `:core:study_unlock` | `PrepareRewardedUnlockOffer`, `StudyUnlockResultStore` | Decides whether a locked surface offers the video (platform supported, `rewarded_ads_enabled` on, server says videos are left today) and preloads it. Hands an earned reward back to the surface that asked for it. |
| `:feature:study_unlock` | `StudyUnlockNavRoute` | The unlock sheet: Subscribe (primary) or "Watch a short video". |
| Day / chapter study coordinators | `start(..., isRewarded)` | Send `reward: true` to `get-day-study` / `get-chapter-study`. A rewarded generation that fails stays "unserved", so the retry is free and asks for no new video. |
| Backend (`bible-planner-api`) | `_shared/rewarded.ts` | Caps rewarded unlocks per user per rolling 24 h across both studies (`ai_rewarded_daily_limit`). Rewarded unlocks never spend the free quota. |

The client reports the reward and the server only caps it; there is no server-side verification
(SSV). Compare `source = 'rewarded'` unlock rows with AdMob's rewards to spot forged rewards, and
add SSV if they clearly diverge.

## 1. AdMob apps

1. Both apps and their **Rewarded** ad unit already exist (see below).
2. They stay on "Limited ad serving" until AdMob verifies them through `app-ads.txt`.
3. Link both apps to the Firebase project (**App settings › Linked services**), so `ad_impression`
   and its revenue reach GA4.
4. Under **Privacy & messaging**, publish a **European regulations (GDPR)** message and a **US state
   regulations** message. UMP shows them; without a published message the consent form never
   appears.

## 2. The IDs

The AdMob account is `quare.software@gmail.com` (publisher `pub-9748272108340789`). Its IDs are not
secret — they ship inside every build — so they live in the code, not in `local.properties`:

| | App ID | Rewarded ad unit (`study_unlock_rewarded`) |
|---|---|---|
| Android | `ca-app-pub-9748272108340789~1160645135` (`androidApp/build.gradle.kts`) | `ca-app-pub-9748272108340789/2517958834` (`AndroidRewardedAdUnitIdProvider`) |
| iOS | `ca-app-pub-9748272108340789~4106372672` (`ADMOB_APP_ID` in `iosApp/Configuration/Config.xcconfig`, read by `Info.plist`) | `ca-app-pub-9748272108340789/2326387144` (`IosRewardedAdUnitIdProvider`) |

**Debug builds always request Google's test ad units**, so the AdMob account is never flagged for
invalid traffic.

The device tests run without `androidApp`, so its App ID never reaches them. `:ui:testing` declares
Google's sample App ID for them instead (see
[Compose UI tests](testing/compose-ui-tests.md#on-an-android-device)).

## 3. Remote Config

| Parameter | Read by | Default | Purpose |
|---|---|---|---|
| `rewarded_ads_enabled` | the app | `true` in the template (`false` in code when Remote Config is unreachable) | Shows `Unlock` instead of `Subscribe`. Turned on for everyone, with no A/B experiment; set it to `false` to hide the video without a release. |
| `ai_rewarded_daily_limit` | the backend | `2` | Rewarded unlocks per user per rolling 24 h, shared by the day and the chapter study. `0` turns them off server-side with no deploy. |

## 4. Before the release that turns the flag on

- **`app-ads.txt`** on the developer website listed in both stores, with the line AdMob shows under
  **Apps › app-ads.txt**.
- **Google Play Console**: "Contains ads" = yes; Data safety updated (advertising ID, device
  identifiers, data shared with Google for advertising).
- **App Store Connect**: privacy labels updated. The Google Mobile Ads SDK ships its own privacy
  manifest. App Tracking Transparency is not requested; ads run without IDFA at a lower eCPM.
- **Privacy policy** mentions AdMob and consent.

## Testing

- Debug builds serve Google's test rewarded ads. Earning a reward works like the real thing.
- The consent form only shows where a message applies. To test it from Brazil, register the device as
  a UMP test device and force the EEA geography (`ConsentDebugSettings`) locally — never commit it.
- The video shows only when the server reports `rewarded_remaining_today > 0`, so the backend with
  rewarded unlocks has to be deployed first.
