---
name: project-ios-kotlin-exception-hook
description: "Crashlytics iOS issue 98f22a15 (KotlinNumber initWithBool, SIGABRT) is the catch-all for unhandled Kotlin exceptions; CrashKiOS hook added in PR #385 splits it into real issues from the next release on."
metadata: 
  node_type: memory
  type: project
  originSessionId: 185eae32-d04c-4495-b0ec-87329e9a3cf3
  modified: 2026-08-30T18:28:08.783Z
---

Crashlytics iOS issue `98f22a155c861019f3b790d4fc4d7456` — titled `-[KotlinNumber initWithBool:]` (SIGABRT) — is not one bug: it is the catch-all bucket for every unhandled Kotlin exception on iOS (Kotlin/Native `terminateWithUnhandledException` abort frames, misattributed symbol). Sampled variants had unrelated causes (draw-time exception inlined at Coil AsyncImagePainter, coroutines escaping on the Compose FlushCoroutineDispatcher after Google login, background LimitedDispatcher worker) with no exception type or Kotlin stack.

PR #385 (merged 2026-08-30, first release after 2.7.1) added `co.touchlab.crashkios:crashlytics:0.9.0` to the iOS target of core/provider/crashlytics; `CrashReporter.configure` now calls `configureUnhandledExceptionReporting()` (expect/actual — iOS runs `enableCrashlytics()` + `setCrashlyticsUnhandledExceptionHook()`, release builds only; Android/desktop no-op). From versions with the hook, these crashes report the real Kotlin exception with symbolicated Kotlin stack traces as separate issues; the old bucket was left OPEN (with a note) until hooked versions dominate. When triaging new iOS crash issues, expect this bucket to shrink and per-cause issues to appear instead. Related: [[project-crashlytics-integration]], [[project-ios-dsym-unrecoverable]].
