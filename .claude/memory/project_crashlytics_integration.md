---
name: project_crashlytics_integration
description: "Firebase Crashlytics integration for Android + iOS, disabled in debug, via core/provider/crashlytics module"
metadata: 
  node_type: memory
  type: project
  originSessionId: dda34483-7d92-467e-b438-4622da0ee90a
---

Firebase Crashlytics lives in `core/provider/crashlytics` (mirrors `core/provider/analytics`): commonMain `CrashReporter` interface (`setCollectionEnabled`, `recordException`) + `crashlyticsModule` with `expect platformCrashlyticsModule`; Android `AndroidCrashReporter` wraps `FirebaseCrashlytics`; iOS actual module is empty and the impl is Swift (`IosCrashReporter` in `iosApp/iosApp/Services/Crashlytics/`) injected via `initializeKoinForIos`; JVM/desktop is a no-op `DesktopCrashReporter`. Registered in `CommonKoinUtils`, exported in `shared/build.gradle.kts`.

**Disabled in debug**: unified through `CrashReporter.setCollectionEnabled(!isDebug)` — Android calls it from `MainApplication` (`KoinPlatform.getKoin().get<CrashReporter>()`, reusing the `FLAG_DEBUGGABLE` flag), iOS from `MainViewController.initializeKoinForIos` with `!Platform.isDebugBinary` (routes to the Koin-registered Swift impl). No separate config function, mirrors `configureRevenueCat`.

**Why / non-obvious:**
- iOS Crashlytics SDK was **already linked via SPM** (firebase-ios-sdk, in the target's Frameworks) — no SPM/Package change was needed, only Swift usage + wiring.
- Android needs the Gradle plugin: `firebaseCrashlyticsPlugin` (`com.google.firebase.crashlytics`) in the catalog, `apply false` in root, applied in `androidApp`. It auto-uploads the R8 `mapping.txt` (release uses `isMinifyEnabled = true`).
- iOS dSYM symbolication: a manual `Upload Crashlytics dSYMs` PBXShellScriptBuildPhase (UUID `D5C0A11C0000000000000001`) runs `"${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"`. Release config is `dwarf-with-dsym`; debug is `dwarf` (script no-ops).
- Firebase project `bible-planner-98ad6`; google-services.json / GoogleService-Info.plist already present.

Related: [[project_remoteconfig_observe_first]], [[project_tester_user_property_flow]], [[project_revenuecat_identity]].
