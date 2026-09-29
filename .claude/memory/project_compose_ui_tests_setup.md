---
name: project-compose-ui-tests-setup
description: "#468 (2026-09-26): Compose UI tests via runComposeUiTest in commonTest on JVM/iOS sim/Android device; the non-obvious traps (D8 names, minSdk 30 trick, Calf native iOS menus, Espresso 3.7)."
metadata:
  node_type: memory
  type: project
  originSessionId: e6ed7346-b6fd-4a5b-b933-b4a33598c6cd
  modified: 2026-09-26T05:27:10.914Z
---

Issue #468 (branch chore/compose-ui-tests) added `*UiTest` classes in commonTest for 7 screens: reading plan, day, day study, books, book details, read, profile. They are wired by `build-logic/.../ComposeUiTests.kt` and the `:ui:testing` module (`setUiTestContent`), and CI runs them in `.github/workflows/ui-tests.yml` (jobs `desktop`, `android`, `ios`) plus `scripts/ui_test_shard.sh`. The `-PuiTests=exclude|only` property splits `jvmTest`: `unit-tests` in build-and-test runs `exclude` (so the coverage report comes from unit tests only, with no loss, since the composables are filtered out anyway), and `desktop` runs `only`. `--tests` can't do this, since it applies only to the task right before it.

**Why:** this was step 2 of 3 porting TuneScout's testing setup; step 3 is E2E (#469).

**How to apply:**
- A module opts into device tests with `src/androidDeviceTest/AndroidManifest.xml`. After that, its **whole commonTest** runs on the device, not only the UI tests.
- Test names must avoid `'` and `,`, since D8 can't represent them. Spaces need DEX 040 (API 30), so build-logic sets `minSdk = 30` only when a requested task ends in `AndroidDeviceTest`. Other options were tried and failed: `android.injected.build.api` is ignored by KMP device tests, and `DexMergingTask` disallows changes. Consequence: `connectedCheck` / `connectedAndroidTest` fail at dexing, so use `connectedAndroidDeviceTest`.
- On iOS the test host has no `LocalUIViewController`, so Calf components crash unless the content is set through `setUiTestContent`. Calf's native `UIMenu` items aren't in the semantics tree, so test only the button that opens a menu.
- Espresso is pinned to 3.7, because the older transitive one crashes on Android 16 (`InputManager.getInstance`).
- The `UiTest` suffix keeps these classes off `testAndroidHostTest`.
- The iOS job caches `~/.konan`, since the first link builds a ~1 GB library cache (~15 min cold). A cold run of the Android shards took ~7.5 min of assembling vs ~3 min on the emulator, so more shards weren't worth it.
- The Kover composable exclusions were kept: without them the merged report would be 66.9%, not 95%.
- Related: [[project-kover-coverage-gate]], [[project-calf-adaptive-components]].
