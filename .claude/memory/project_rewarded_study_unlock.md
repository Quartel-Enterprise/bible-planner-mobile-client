---
name: project-rewarded-study-unlock
description: "#508 AdMob rewarded video unlocks an AI study — design decisions, deploy order, and what still needs console/store setup (started 2026-10-02)"
metadata:
  node_type: memory
  type: project
  originSessionId: 45fb6edb-f91a-4bde-a103-eeb6aaf55c43
  modified: 2026-10-03T02:42:16.534Z
---

Issue #508 (branch `feature/rewarded-ad-unlock` in both the mobile client and `bible-planner-api`, worktrees under `.claude/worktrees/rewarded-ad-unlock`) adds a rewarded-video unlock for free users with no free AI study left.

- **Sheet is a feature route, not a shared ui component** (deviates from the issue text): `:feature:study_unlock` (`StudyUnlockNavRoute`) owns the whole ad flow; surfaces only swap Subscribe→Unlock and, on `StudyUnlockResultStore.observeEarned(requestKey)`, run their normal generation with `isRewarded = true`. Request keys are surface-specific (`day_study_card|…`, `day_study_detail|…`, `chapter_study|…`, `reader_chapter_study|…`, `day_reading_complete[_banner]|…`) so two live ViewModels never both react.
- **Free retry after a failed rewarded generation** lives in the coordinators (`hasUnservedReward`), dropped on success or on a 402.
- **Deploy order:** the client's status DTOs *require* `rewarded_remaining_today`, so the backend PR must be merged/deployed before any app build hits prod. All four PRs merged 2026-10-03 in that order (api #37 deployed + migration verified, landing #4, app #511, #512); ships in 2.11.0.
- Kill switches: client RC `rewarded_ads_enabled` (published `true` on 2026-10-03, RC v37, no A/B test by the user's choice; code default false), server RC `ai_rewarded_daily_limit` (= 2, RC v38; code default 2, 0 = off). No SSV; monitor `source='rewarded'` rows vs AdMob rewards.
- AdMob setup done 2026-10-03: both apps + `study_unlock_rewarded` units, GDPR + US-states UMP messages published, apps linked to Firebase `bible-planner-98ad6` with impression-level ad revenue on. pierrevieiraggg@gmail.com is an AdMob admin of the Quare account because linking needs Firebase Owner + GA4 editor (quare.software is only Firebase Editor). Apps stay on limited serving until app-ads.txt (landing page PR #4) is live.
- iOS: Swift `IosRewardedAdDataSource`/`IosAdsConsentDataSource` passed through `initializeKoinForIos`; GoogleMobileAds SPM 13.11 (bundles UMP 3.1); AdMob account quare.software@gmail.com (pub-9748272108340789); App IDs and ad unit IDs are committed in code (public), not in local.properties/LOCAL_PROPERTIES. Android: play-services-ads 25.5.0 + UMP 4.0.0.

**Why:** keeps the ad logic in one ViewModel instead of six, and honors the issue's "retry after video is free".
**How to apply:** when touching study surfaces, keep the rewarded path symmetrical with the free path; see [[project-loadable-per-field-loading]] style for state, and docs/setup_admob.md for the console/store checklist the user still has to do.

**Store disclosures (2026-10-03):** Play Data safety is versioned as `fastlane/metadata/android/data_safety.csv` (Play Console export format) and posted by the `android upload_data_safety` lane on production releases only (PR #512); the Data safety API has no GET, so to change it re-export from the console, edit, commit. Still manual at release time: Play "Contains ads" and App Store App Privacy labels (no API for either).
