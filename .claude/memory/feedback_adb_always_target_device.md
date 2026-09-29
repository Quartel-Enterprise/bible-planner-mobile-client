---
name: adb-always-target-device
description: "never run a bare `adb shell settings put` / adb command — a bare adb hits the user's own emulator; always pass -s (and the task's private adb server)"
metadata:
  node_type: memory
  type: feedback
  originSessionId: 30c0a56e-c670-4266-bc07-1d14537ca708
  modified: 2026-09-27T00:59:28.848Z
---

Always pass `-s <serial>` to adb, and use the task's private adb server when the task has one. Never run a bare `adb ...`, least of all a command that changes settings.

**Why:** on 2026-09-26 an E2E session ran a bare `adb shell settings put global {window,transition,animator_duration}_animation_scale 0` before it switched to its own task emulator. That hit the user's Medium_Phone and turned off every animation there, including Compose and Nav3 transitions. Nothing restored it, so a later session found the setting still off and had to track down the cause.

**How to apply:** only zero the animation scales on the task's own emulator (see [[task-emulator]]). If you ever change a setting on a device the user shares, put it back when you're done.
