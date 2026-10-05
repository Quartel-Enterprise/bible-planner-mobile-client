---
name: project-headless-jvm-tests
description: "JVM test workers that load AWT (Compose jvmTest) became macOS Dock apps and stole keyboard focus; fixed with java.awt.headless=true in the Compose convention (#499)."
metadata:
  node_type: memory
  type: project
  originSessionId: 79057aee-722f-44a2-8827-285cc6e15af1
  modified: 2026-09-29T18:35:27.115Z
---

Local `jvmTest` on macOS made each Compose module's test worker (`GradleWorkerMain`, "Gradle Test Executor N") register as an app. You got a row of Duke icons in the Dock, and the frontmost one took the keyboard focus. PR #499 (merged 2026-09-29) sets `java.awt.headless=true` on every `Test` task in `configureComposeUiTests()` (ComposeUiTests.kt).

**Why:** it lives in the Compose convention, not the KMP one, because `:shared` applies only the Compose convention. It is also the module with the E2E UI tests. CI's Linux runners have no display, so they were already headless, and local runs now match CI.

**How to apply:** if Duke icons reappear, find the culprit with `lsappinfo list` while the tests run and `ps -o args= -p <pid>`. A module whose test task skips the Compose convention is the usual cause. The fallback, if headless ever breaks a test, is `-Dapple.awt.UIElement=true`, which keeps AWT but hides the Dock icon and doesn't activate. The failures from `testAndroidHostTest` (NPE in Settings() with no Context) predate this change and have nothing to do with headless.
