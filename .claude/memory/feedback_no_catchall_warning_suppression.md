---
name: feedback-no-catchall-warning-suppression
description: "Don't silence build warnings with broad catch-all switches (e.g. android.sync.suppressAgpWarnings=GENERIC); fix the cause or use a narrow opt-out."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 86405e43-53aa-442c-98ae-43d08e34b2d2
  modified: 2026-09-15T02:46:31.745Z
---

The user rejected `android.sync.suppressAgpWarnings=GENERIC` (PR #448) because it would also hide every future AGP warning in that category. Narrow, warning-specific switches (e.g. `kotlin.native.ignoreDisabledTargets=true`, per-module `kotlin.mpp.applyDefaultHierarchyTemplate=false`) were fine.

**Why:** a catch-all suppression trades one known warning for blindness to unknown future ones.

**How to apply:** when a build warning shows up, first find the condition that fires it (read the plugin sources in ~/.gradle/caches, e.g. AGP's `KotlinMultiplatformAndroidPlugin.checkForMissingConfigurations` only checks that `src/commonTest` exists with no host test compilation) and fix that. If you still suggest a suppression, name its blast radius. Related: [[feedback-ktlint-custom-enforcement]].

AGP note: `withHostTest {}` must be declared per module. Calling it again from the convention plugin recreates the host test options and drops the screenshot modules' `isIncludeAndroidResources`/`isReturnDefaultValues`.
