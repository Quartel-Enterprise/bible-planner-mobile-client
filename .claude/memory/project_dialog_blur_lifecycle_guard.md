---
name: dialog-blur-lifecycle-guard
description: "iOS device leaks CMP dialog compositions on camera→push-crop; global blur state must be lifecycle-driven, not onDispose-only"
metadata: 
  node_type: memory
  type: project
  originSessionId: ab3d07fc-3097-497b-b762-51450943f1b0
  modified: 2026-08-11T19:14:27.980Z
---

On real iOS devices (not reproducible in the simulator, even with a fullscreen-modal + push-on-dismissal-completion harness), pushing a route over a Nav3 dialog-scene right after a camera dismissal can leak the CMP dialog's composition: `onDispose` inside the dialog never runs. Any app-global state set from inside a dialog composition (e.g. `WindowBlurController.radius` via `WindowContentBlurEffect`) then sticks forever — the crop screen stayed blurred.

Fix pattern (2026-08-11, branch fix/camera-crop-blur-stuck): `WindowContentBlurEffect` also observes the nav entry's `Lifecycle` (owned by the main composition via nav3 scene/decorators) with a `LifecycleEventObserver` — ON_STOP/ON_DESTROY force radius 0, ON_START restores it. iOS fires ON_STOP when a fullscreen modal (camera) covers the app and ON_START on dismissal, so blur also self-heals across backgrounding. Same commit restored camera-launch feedback (CameraLaunchOverlay scrim+spinner in ProfilePhotoPickers, FileKit onError overload resets it).
