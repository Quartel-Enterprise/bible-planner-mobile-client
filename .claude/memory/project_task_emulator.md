---
name: project-task-emulator
description: "Each task gets its own BiblePlanner_<short_desc> AVD (PR #476, ported from TuneScout #96); boot on private adb port, am instrument not connected*, shut down after use, finish-task sweeps AVDs without a branch."
metadata:
  node_type: memory
  type: project
  originSessionId: 654fb753-320c-413c-8dad-94948185d6c8
  modified: 2026-09-26T14:55:58.701Z
---

Since PR #476 (2026-09-26), `start-task` creates a per-task AVD `BiblePlanner_<short_description>` (config.ini copied from `Pixel_9`); procedure lives in `.claude/skills/task-emulator.md`. Ported from TuneScout's #96 flow.

**Why:** parallel Claude sessions all see every emulator; `connectedAndroidDeviceTest` installs on all of them, clobbering other sessions' / the user's devices.

**How to apply:** for device work boot the task AVD on console port 5700–5798 with `ANDROID_ADB_SERVER_PORT=port+1000` (invisible to the default adb server), install the module's self-instrumenting `<namespace>.test` APK and `am instrument` (exit code is 0 even on failure — read `FAILURES!!!`). Shut it down right after device work (user worried about RAM). A booted AVD ≈2.3 GB disk; `finish-task` deletes every `BiblePlanner_*` AVD with no matching local branch. Never touch `Pixel_9`/`Medium_Phone`/`Pixel_Tablet`/`TuneScout_*`. Related: [[project-compose-ui-tests-setup]].
