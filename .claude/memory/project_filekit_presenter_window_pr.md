---
name: filekit-presenter-window-pr
description: "Pending upstream PR vinceglb/FileKit#639 (camera presented from dedicated window by default); when released, drop the app-side CameraPresenterWindow"
metadata: 
  node_type: memory
  type: project
  originSessionId: ab3d07fc-3097-497b-b762-51450943f1b0
  modified: 2026-08-11T21:27:23.778Z
---

Upstream PR vinceglb/FileKit#639 (fixes issue #638, opened 2026-08-11 from the clone at /Users/pierrevieira/StudioProjects/FileKit) makes `openCameraPicker` present from a FileKit-managed transparent key `UIWindow` when no presenter is passed — the CMP 1.11+ touch-corruption fix. Until it ships in a FileKit release, the app carries the same recipe locally: `CameraPresenterWindow` in `feature/edit_profile/src/iosMain/.../CameraPicker.ios.kt` passed via `FileKitOpenCameraSettings(presenter = ...)`. Once the app bumps to a FileKit version containing #639, that app-side class and the settings argument can be deleted (plain `rememberCameraPickerLauncher` suffices). Related: [[dialog-blur-lifecycle-guard]].
