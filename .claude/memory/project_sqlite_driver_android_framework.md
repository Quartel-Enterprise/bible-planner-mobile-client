---
name: project_sqlite_driver_android_framework
description: "Android uses AndroidSQLiteDriver (framework SQLite), not BundledSQLiteDriver, to avoid the libsqliteJni.so missing-ABI-split crash; the driver dep must be mirrored in both room and shared modules."
metadata: 
  node_type: memory
  type: project
  originSessionId: 502dc398-05f8-4ec5-9462-1b8691d58249
---

Crashlytics issue 9775ecb7 (`UnsatisfiedLinkError: dlopen failed: library "libsqliteJni.so" not found`, FATAL, seen 1.18.1–1.19.1 on Android 12 Pixels) was caused by the bundled SQLite native lib (`libsqliteJni.so`, from `androidx.sqlite:sqlite-bundled` / `BundledSQLiteDriver`) being absent at runtime — a missing AAB ABI **config split** (cloned-app / custom-ROM / non-Play installs strip splits). Not related to the Room 3 migration; the bundled driver predated it.

**Resolution:** Android uses `AndroidSQLiteDriver` (from `androidx.sqlite:sqlite-framework`, OS-provided SQLite → no native lib to load), while iOS/desktop keep `BundledSQLiteDriver`. The driver is now set per-platform inside each `getDatabaseBuilder()` (androidMain/iosMain/desktopMain of `core/provider/room`), not in the common `RoomModule` (`.setDriver` was removed from commonMain). Safe here because the schema is plain relational (no FTS/JSON1/virtual tables/pragmas).

**Gotcha — mirror the dep in TWO modules:** the sqlite driver dependency exists in BOTH `core/provider/room/build.gradle.kts` AND `shared/build.gradle.kts` (the `shared` umbrella re-declares it so the app packages the native lib, since room uses `implementation`). So any SQLite driver change must be applied in both: android→`androidx.sqlite.framework`, iosMain/jvmMain(desktopMain)→`androidx.sqlite.bundled`. `desktopApp/build.gradle.kts` also declares bundled. Verify with: build `:androidApp:assembleDebug`, then `unzip -l <apk> | grep libsqliteJni.so` (must be gone) and grep dex for `AndroidSQLiteDriver` (must be present).

Note: worktree builds need `local.properties` and `androidApp/google-services.json` copied from the main repo (both gitignored). Related: [[project_crashlytics_integration]], [[project_ios_dsym_unrecoverable]].
