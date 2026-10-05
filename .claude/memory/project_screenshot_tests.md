---
name: project-screenshot-tests
description: "Oct/2026 Roborazzi screenshot tests ported from TuneScout, per-feature in androidHostTest with ATF a11y checks failing on Error; own mode property because store generators force roborazzi.test.record."
metadata:
  node_type: memory
  type: project
  originSessionId: 79f2eb41-b210-43c4-8e3a-43e9776be9fb
  modified: 2026-10-04T01:34:58.996Z
---

Branch enhancement/screenshot-tests (2026-10-03): screenshot tests ported from TuneScout, adapted. Not a central :tools module (feature isolation, see [[project-feature-isolation]]); each feature has `*ScreenshotTest` in its androidHostTest, base class in `:ui:screenshots:testing` (named so the `:.*:testing` graph rule keeps it test-only), wired by `configureScreenshotTests()` in build-logic.

- Mode comes from `-PscreenshotTests=verify|record`, which becomes the `screenshotTests.mode` system property and an explicit RoborazziTaskType. The store-screenshot modules set `roborazzi.test.record=true` on the whole host test task, so Roborazzi's own properties can't be used.
- References are recorded on Linux only. `screenshot-tests` verify never skips, because merge-when-green reads a skipped check as passed. Recording is a separate `record-screenshots` workflow (labeled-only), which pushes with MERGE_WHEN_GREEN_TOKEN (a GITHUB_TOKEN push skips the required check-translations) and drops the label only after the push.
- `*ScreenshotTest` classes are excluded when the requested task ends in `Screenshots` (store/README generation). AGP allows a single `withHostTest`, so the convention only checks `isIncludeAndroidResources` via `variant.hostTests` and fails if it's missing.
- The ATF check fails on Error. User chose (2026-10-03) to fix the existing a11y errors in the same PR: books sort/filter labels, 48dp toggles, labeled reading-plan day toggle.
- Robolectric keeps the previous orientation, so every capture sets the full Pixel7 + `+port`/`+land` qualifiers.
- The reading plan dropdown popup doesn't capture (blank, list scrolled), so that test was dropped.

**Why:** the first pilot found a real truncation bug (testament toggle at 1.5x/2x font) and unlabeled buttons on its first run.
**How to apply:** new screens get a `*ScreenshotTest` plus the record label; never commit locally recorded PNGs.
