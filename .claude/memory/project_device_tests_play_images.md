---
name: project-device-tests-play-images
description: "PR #547 (Oct/26) — device tests on Google Play images need an AdMob App ID (lives in :ui:testing's manifest; KMP ignores generated device-test manifests) and the E2E click helper reveals enterAlways bars."
metadata:
  node_type: memory
  type: project
  originSessionId: 98a67077-34b2-4221-b784-670c12e4844d
  modified: 2026-10-04T04:06:15.351Z
---

Device test APKs that pull the ads SDK (anything depending on core:study_unlock) crash with "Missing application ID" from MobileAdsInitProvider on Google Play images; CI's API 35 `default` image never trips it. Fix (PR #547, merged 2026-10-04): `ui/testing/src/androidMain/AndroidManifest.xml` declares Google's sample App ID with `tools:replace` — every device test APK includes :ui:testing.

Dead end worth remembering: build-logic can't inject manifest entries into KMP device tests — AGP 9.4.1's `KmpComponentImpl` hard-codes `manifestOverlayFiles = emptyList()`, so `sources.manifests.addGeneratedManifestFile` is silently ignored.

Second finding: on a Pixel 9-sized landscape window, `performScrollTo` scrolls through nested scroll and collapses the read screen's `enterAlways` top bar; its Back node stays in the tree with zero bounds and clicks land nowhere. E2eInteractions.click now swipes the node's own screen lists down (never inside a dialog/popup) and fails loudly if the node still has no size.

**Why:** both bugs were invisible on CI's emulator and only showed on task emulators copied from Pixel_9 ([[project-task-emulator]]).
**How to apply:** when device tests fail locally but pass in CI, suspect Play-image/device-size differences first; see [[project-compose-ui-tests-setup]] and [[project-e2e-lazy-scroll-fallback]].
