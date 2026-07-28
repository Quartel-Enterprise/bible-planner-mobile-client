---
name: project-desktop-jlink-modules
description: Desktop jlink image needs explicit modules(); Homebrew JDK is NOT the cause of desktop packaging crashes and packages fine with the vendor-check bypass.
metadata: 
  node_type: memory
  type: project
  originSessionId: 43662096-58a9-4ff7-97e4-114bc7b8d6ee
  modified: 2026-07-27T23:40:01.466Z
---

The packaged desktop runtime is a trimmed jlink image, so anything outside the auto-detected module set is simply absent at runtime. `modules(...)` in `desktopApp/build.gradle.kts` carries the output of `./gradlew :desktopApp:suggestRuntimeModules` (jdeps). Missing `jdk.unsupported` caused the 2.2.0 `NoClassDefFoundError: sun/misc/Unsafe` at `MessageSchema:493` (Sentry BIBLE-PLANNER-DESKTOP-3/-4) — DataStore's bundled protobuf needs `sun.misc.Unsafe`. It surfaced as two Sentry issues because it throws on two threads (Dispatchers.IO and AWT-EventQueue/Dispatchers.Main). The app does not terminate; it keeps running with persistence silently broken.

Ruled out (verified 2026-07-26): the Homebrew JDK is **not** implicated. The same Homebrew JDK 21.0.9 runs the code fine when the module is present, and `packageDmg` produces a valid 134 MB DMG with `-Pcompose.desktop.packaging.checkJdkVendor=false`. That flag only silences the compose-multiplatform#3107 vendor warning; it does not affect the module set. Don't chase the JDK vendor when a desktop `NoClassDefFoundError` appears — re-run `suggestRuntimeModules` first.

Merged as PR #302 (2026-07-27). The release workflow has a `desktop-build` job — one runner per OS, since jpackage only builds the host system's installers — producing dmg/msi/deb/rpm, which `finalize` attaches to the GitHub release. It asserts the packaged runtime still carries the declared modules, so this regression fails the release instead of shipping. `platforms` is `all | mobile | android | ios | desktop`; `scripts/release/release.sh` and `release-prod.sh` dispatch that value, so they must change together. The installers are unsigned, x86_64-only, and carry a glibc floor from the runner.

**Compose desktop build layout** (cost a broken Linux release before it was caught): `binaries/main/app` only exists on macOS — `packageDeb`/`packageRpm` go straight from the jlink output to the package. The runtime image every OS embeds is `desktopApp/build/compose/tmp/main/runtime`, so anything inspecting the runtime must search from `compose/`, not from `binaries/`. Each format lands in its own `binaries/main/<id>/` directory (`deb`, `rpm`, `dmg`, `msi`), which is why the workflow collects installers into a flat dir before uploading.

The Linux leg is testable locally without CI: `git archive HEAD` into a temp dir, then run the build in an `ubuntu:24.04` container with `openjdk-21-jdk fakeroot binutils rpm` (Debian ships rpmbuild in the `rpm` package). Installing the deb there fails at `postinst` unless `/usr/share/applications` and `/usr/share/desktop-directories` exist — a bare-container artifact, not a package defect. Note CI jobs all `checkout ref: main`, so a `workflow_dispatch` from a feature branch tests that branch's YAML against main's code. Related: [[project-desktop-analytics-measurement-protocol]].
