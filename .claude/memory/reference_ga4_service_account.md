---
name: reference_ga4_service_account
description: How to query GA4/Firebase Analytics data via the Data API — service account ga4-reader on property 516768577.
metadata:
  node_type: memory
  type: reference
  originSessionId: ceec2f42-56e8-44a1-a599-1e22b73f08cd
  modified: 2026-09-29T17:06:36.287Z
---

The Firebase MCP does **not** expose Analytics data, and there is no BigQuery export. `gcloud auth application-default login --scopes=...analytics.readonly` is **blocked** by Google ("This app is blocked"). Don't keep trying that path.

What works:
- Service account `ga4-reader@bible-planner-98ad6.iam.gserviceaccount.com`, with the **Viewer** role on GA4 property **516768577** (account 377953152).
- The `analyticsdata` and `analyticsadmin` APIs are enabled in project `bible-planner-98ad6`.
- Authenticate with `google.oauth2.service_account` + `AuthorizedSession` (the system `google-auth` is enough), calling `POST .../v1beta/properties/516768577:runReport`.
- The JSON key lives in the session scratchpad and **is lost between sessions**. When that happens, create a new one with `gcloud iam service-accounts keys create` and delete the orphan with `keys delete`. The service account and its permission stay valid.

To measure exposure, use `userEngagementDuration` (foreground time), never `averageSessionDuration`, which includes background time and once showed 36 min where the real figure was 3.8. Filter `platform` to Android/iOS: the web stream mixes the website and desktop.

Since 2.6.0 (#349), `unifiedScreenName` carries per-destination names (`plans`, `books`, `profile`, `read`, `day`...) from `NavRouteToDestinationMapperImpl`, which makes per-screen time measurable.
