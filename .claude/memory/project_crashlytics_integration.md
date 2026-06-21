---
name: project_crashlytics_integration
description: "Firebase Crashlytics integration for Android + iOS, disabled in debug, via core/provider/crashlytics module"
metadata: 
  node_type: memory
  type: project
  originSessionId: dda34483-7d92-467e-b438-4622da0ee90a
---

Firebase Crashlytics lives in `core/provider/crashlytics` (mirrors `core/provider/analytics`): commonMain `CrashReporter` interface (`setCollectionEnabled`, `recordException`) + `crashlyticsModule` with `expect platformCrashlyticsModule`; Android `AndroidCrashReporter` wraps `FirebaseCrashlytics`; iOS actual module is empty and the impl is Swift (`IosCrashReporter` in `iosApp/iosApp/Services/Crashlytics/`) injected via `initializeKoinForIos`; JVM/desktop is a no-op `DesktopCrashReporter`. Registered in `CommonKoinUtils`, exported in `shared/build.gradle.kts`.

**Startup config**: a single `CrashReporter.configure(isDebug)` extension (commonMain) does `setCollectionEnabled(!isDebug)` AND, when not debug, registers a `CrashReporterLogWriter` on the global kermit `Logger`. Called from `MainApplication` (`KoinPlatform.getKoin().get<CrashReporter>().configure(isDebug = ...)`, reusing `FLAG_DEBUGGABLE`) and from `MainViewController.initializeKoinForIos` (`!Platform.isDebugBinary`, routes to the Koin-registered Swift impl). So crashes/non-fatals are collected only in release.

**Non-fatal reporting (recordException)**: NOT called directly at most sites. Instead `CrashReporterLogWriter` forwards every kermit log at `Severity.Error` **that carries a throwable** to `CrashReporter.recordException`. Since `Logger.withTag(...)` shares the global mutable config, any `Logger.e(throwable) { ... }` across the codebase auto-reports (sync layer, iOS background download bridge, iOS Koin-init catch, etc.). Silent swallow sites were updated to log the throwable so they flow through: `DownloadChaptersUseCase`, `PlanLocalDataSource`, `BibleVersionsLocalDataSource`, `BibleVersionsRemoteDataSource`. To report a new error: just `Logger.e(throwable) { msg }` — don't inject `CrashReporter`. ViewModel user-facing failures (login/purchase/logout) are intentionally NOT wired yet (need expected-error filtering first).

**Why / non-obvious:**
- iOS Crashlytics SDK was **already linked via SPM** (firebase-ios-sdk, in the target's Frameworks) — no SPM/Package change was needed, only Swift usage + wiring.
- Android needs the Gradle plugin: `firebaseCrashlyticsPlugin` (`com.google.firebase.crashlytics`) in the catalog, `apply false` in root, applied in `androidApp`. It auto-uploads the R8 `mapping.txt` (release uses `isMinifyEnabled = true`).
- iOS dSYM symbolication: a manual `Upload Crashlytics dSYMs` PBXShellScriptBuildPhase (UUID `D5C0A11C0000000000000001`) runs `"${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"`. Release config is `dwarf-with-dsym`; debug is `dwarf` (script no-ops).
- Firebase project `bible-planner-98ad6`; google-services.json / GoogleService-Info.plist already present.

Related: [[project_remoteconfig_observe_first]], [[project_tester_user_property_flow]], [[project_revenuecat_identity]].
