---
name: play-sa-store-presence
description: "Play service account needs \"Manage store presence\" for listing/screenshot uploads — release permissions alone don't cover it"
metadata: 
  node_type: memory
  type: project
  originSessionId: 839705bb-6ec5-48f4-b458-6ccf18407b0c
  modified: 2026-09-01T04:09:03.036Z
---

The Play Console service account `play-ci-publisher@bible-planner-98ad6.iam.gserviceaccount.com` originally had only release permissions (release to production, testing tracks), which let AAB uploads work while `deliver` screenshot uploads failed at `edits.validate` with "The caller does not have permission" — the per-locale image uploads into the edit succeed, the failure only surfaces at validate/commit. Listing changes (screenshots included) require the separate app-level permission **"Manage store presence"**, granted 2026-09-01 (Users and permissions → play-ci-publisher → App permissions → Bible Planner). Play has no screenshots-only granularity; this permission also covers pricing, in-app products, distribution info and promotions. Validated by the `store screenshots` workflow's validate-only dispatch (run 33468538014).
