---
name: project-play-link-ga4-confirmed
description: The Firebase↔Google Play link is confirmed delivering Android in_app_purchase revenue to GA4, stamped on the export date (~48h late), not the purchase date.
metadata:
  type: project
---

Verified on 2026-08-26 in the Firebase Analytics Events report (property `bible-planner-98ad6`, Events → secondary dimension Platform): the 2026-08-22 Play sale of R$16.90 arrived as a single `in_app_purchase` event with **Platform = Android** and **Total revenue $3.29**, so the Google Play link is alive and the split arrangement in [[project-desktop-analytics-measurement-protocol]] / `docs/analytics/README.md` (Android = Play link, iOS = RevenueCat→GA4) holds.

Non-obvious detail: the event is dated **Aug 24**, not Aug 22 — the Play export stamps the event on its own delivery date, so GA4 Android revenue lands ~48h after the purchase and is attributed to the wrong day. Don't read a same-day zero as a broken link; wait 3+ days before concluding anything.

Also confirmed by the same breakdown: no iOS `in_app_purchase` had arrived yet as of Aug 25 — the RevenueCat→GA4 iOS integration was only configured Aug 24 (there is a GA4 annotation on that date saying pre-Aug-24 iOS revenue is *absent, not zero*; full history lives in RevenueCat). See [[project-revenuecat-identity]].
