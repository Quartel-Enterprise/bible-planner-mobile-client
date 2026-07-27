---
name: project-desktop-jlink-modules
description: Desktop jlink image needs explicit modules(); Homebrew JDK is NOT the cause of desktop packaging crashes and packages fine with the vendor-check bypass.
metadata: 
  node_type: memory
  type: project
  originSessionId: 43662096-58a9-4ff7-97e4-114bc7b8d6ee
  modified: 2026-07-26T23:41:01.697Z
---

The packaged desktop runtime is a trimmed jlink image, so anything outside the auto-detected module set is simply absent at runtime. `modules(...)` in `desktopApp/build.gradle.kts` carries the output of `./gradlew :desktopApp:suggestRuntimeModules` (jdeps). Missing `jdk.unsupported` caused the 2.2.0 `NoClassDefFoundError: sun/misc/Unsafe` at `MessageSchema:493` (Sentry BIBLE-PLANNER-DESKTOP-3/-4) — DataStore's bundled protobuf needs `sun.misc.Unsafe`. It surfaced as two Sentry issues because it throws on two threads (Dispatchers.IO and AWT-EventQueue/Dispatchers.Main). The app does not terminate; it keeps running with persistence silently broken.

Ruled out (verified 2026-07-26): the Homebrew JDK is **not** implicated. The same Homebrew JDK 21.0.9 runs the code fine when the module is present, and `packageDmg` produces a valid 134 MB DMG with `-Pcompose.desktop.packaging.checkJdkVendor=false`. That flag only silences the compose-multiplatform#3107 vendor warning; it does not affect the module set. Don't chase the JDK vendor when a desktop `NoClassDefFoundError` appears — re-run `suggestRuntimeModules` first.

The release workflow now has a `desktop-build` job (one runner per OS, since jpackage only builds the host system's installer) that attaches the dmg/msi/deb to the GitHub release, and asserts the packaged runtime still carries the declared modules so this regression fails the release instead of shipping. Its `platforms` input was renamed `both` → `all`; `scripts/release/release.sh` and `release-prod.sh` dispatch that value, so they must change together. The installers are unsigned — no code signing or notarization yet. Related: [[project-desktop-analytics-measurement-protocol]].
