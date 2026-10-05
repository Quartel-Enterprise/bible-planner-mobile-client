---
name: project_ad_monetization_analysis
description: "Ad monetization analyses (Aug and Sep/2026) — banners don't pay; rewarded re-approved in Oct/2026 (#508) for UK/US paid traffic, with the cached chapter study and a server-side daily cap."
metadata:
  node_type: memory
  type: project
  originSessionId: ceec2f42-56e8-44a1-a599-1e22b73f08cd
  modified: 2026-09-29T17:23:44.176Z
---

Two analyses, both with real GA4 and RevenueCat data (project `projf7de3053`), concluding that **ads don't pay at the current scale**.

**2026-08-15, banner + interstitial across the whole app:** R$50–127/month (baseline R$81). Inputs: 179 DAU, 1.43 sessions per DAU/day, 3.8 min of engagement per session. The inventory bottleneck is the number of *sessions*: a cap of 2 interstitials per user/day is unreachable.

**2026-09-29, banner only on the 3 main tabs (plans/books/profile), above the bottom bar:** **R$4–26/month (baseline R$12)**, ARPDAU R$0.06. At that rate, AdMob's US$100 payout would take ~3.5 years. The tabs are *hubs*: together they hold 27.6% of engagement (plans 31 s per visit, books 9 s, profile 20 s), while `read` holds 48%. Inputs: 200 mobile DAU, 4 subscribers (MRR R$61). Adding a banner to the `read` screen too (234 h/month, 57 s per visit, 78% Brazil and therefore lower eCPM): **R$10–49/month (baseline R$23)**, about 1.5 subscribers' worth. The catch is that it reaches the 664 active readers, exactly the Pro candidates.

**Why:** implementation is expensive — there is no multiplatform AdMob SDK for CMP (it needs `AndroidView`/`UIKitView` interop, and on iOS the tab bar is a native `UITabBar` via Calf), plus UMP, ATT and the privacy manifest. Desktop is unfeasible: AdMob has no JVM SDK. On iOS (~35% of the time), users who deny ATT drag the eCPM down.

**How to apply:** a single extra subscriber is worth more than the whole banner. If the topic comes back, a banner is only justified as a *conversion lever* ("remove ads" as a Pro benefit), measured by an experiment, not as revenue. The deciding number is DAU (revisit from ~2–3k). **Rewarded ads to unlock AI studies don't pay either (analyzed 2026-09-29).** The free quota is 3 lifetime studies (Remote Config `ai_study_free_limit`). Only 24 active users were at the limit, totaling 317 reading days/month, so the maximum inventory is ~317 views. Each view earns ~US$0.0035, but 86% of unlocks trigger a new Gemini study at ~US$0.008, a cost that doubles on 2027-01-01. Result: net **−R$1 to −R$9/month**, and the format also cannibalizes the paywall's hottest leads. If the goal is engagement, the alternatives without an ads SDK are a renewing quota or earning studies through reading streaks. **User's decision: issue #497**, which keeps the 3 lifetime studies and grants **+1 per rolling 7-day window once they run out**. The server returns an effective `used_count` (`free_limit - available`) so old app versions show the renewal without an update, and `ai_study_weekly_free_limit = 0` turns the rule off without a deploy.

**Revision on 2026-10-02 (issue #508):** rewarded was re-approved for Android and iOS, covering the chapter study, the day study and the day_reading_complete card. Two facts changed the math. First, paid traffic is planned for the UK and US, where rewarded eCPM in non-game apps is ~US$8–15 (~US$0.01 per view). Second, `ai_chapter_studies` is a global cache per chapter × version × language, and generating the full corpus for one combination costs only ~US$10, which zeroes the marginal cost. Decisions: AdMob only, because of the Firebase/GA4 link (MoPub no longer exists), behind our own wrapper in `core/provider/ads`. The app tells the server about the reward, and the server enforces a 24 h cap (`ai_rewarded_daily_limit`). The user chose this over SSV: abuse is bounded by the cap itself, and SSV is a follow-up if unlocks exceed the rewards in the AdMob report. The launch is an A/B experiment with Subscribe as the primary action.

How to pull the data: [[reference_ga4_service_account]].
