---
name: project-app-store-screenshots-ios-sim
description: "App Store screenshots are captured on the iOS simulator (PR #479) and framed by store-screenshots under Robolectric; Play stays Robolectric-only; separate Play/AppStore Gradle tasks."
metadata:
  node_type: memory
  type: project
  originSessionId: 8d1a2c9c-fbcf-5408-85ed-69cfdbaf2ed1
  modified: 2026-09-27T00:02:36.956Z
---

Since PR #479 (merged 2026-09-27) the App Store listing shots are rendered on the iOS simulator, so they show SF Symbols/Calf/SF font, and the Play shots stay on Robolectric. The store-screenshots lib is an Android-only AAR, so it can't run on iOS. It still draws the frames: each iPhone/iPad class draws the captured PNG instead of the screen.

Non-obvious bits:
- iOS test binaries only bundle the Compose resources of *production* deps. A test-only dep like :feature:day → :feature:day_study needs its resources copied into `copyTestComposeResourcesForIosSimulatorArm64` by hand (done in feature/day/build.gradle.kts).
- Deciding whether the Apple classes run keys off the requested task names (`*AppStoreScreenshots` or `collectStoreScreenshots`), like the device-test minSdk trick. Plain `testAndroidHostTest` skips them.
- The capture sizes in `AppleScreenshotSlot` must match the lib's frame screen (logical slot size). Recheck them after a store-screenshots upgrade.
- On iPad the 1024pt capture shows real wide layouts; the chat shot shows an empty history sidebar (left as-is).

**Why:** the user wanted App Store shots to match the new native iOS look, kept separate from Play.
**How to apply:** a new listing screen needs commonTest content + an iosTest `*AppleScreenshotCaptures` + `configureAppleStoreScreenshots(...)`; see docs/store-listing-screenshots.md. Related: [[project-calf-adaptive-components]], [[project-ui-icons-sf-symbols]].
