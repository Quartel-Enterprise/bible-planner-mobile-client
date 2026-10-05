---
name: project-baseline-profile-evaluated
description: Oct/2026 :tools:baseline-profile (ported from TuneScout) — app profile gives −12% cold start on the A56 over library-only profiles; frames unchanged; run only on a physical device with leaveApksInstalledAfterRun.
metadata:
  node_type: memory
  type: project
  originSessionId: 543b4f53-62eb-4ed0-bec3-b197b2de7b06
  modified: 2026-10-04T05:00:44.178Z
---

2026-10-04, branch enhancement/baseline-profile: ported TuneScout's module as `:tools:baseline-profile` (macrobenchmarks + generator). Journeys find screens only by test tags (`plans_list`, `plan_day`, `*_tab`, `day_content`, `day_passages`, `read_chapters`, `books_content`) exposed via testTagsAsResourceId in MainActivity, so any device language works. Numbers live in docs/performance.md.

Before it, the merged release profile had zero app/Koin/Ktor/Supabase/Room rules (library profiles only, profileinstaller transitive). On the Galaxy A56 the app profile + startup profile (dexLayoutOptimization) cut cold start 349→306 ms (−12%), beating even full `speed` AOT (355 ms); frames showed no measurable change vs library-only. Macrobenchmark None-vs-Partial: startup −26%, P99 frames roughly halved.

**Why it matters:** the user initially wanted the module as a measurement tool, not just for the profile — don't dismiss it on profile gain alone (I first recommended against it from adb numbers and was corrected).

**How to apply:**
- Run only on a physical device (emulator frame data is pure noise) with `ANDROID_SERIAL` and `-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true`, else Gradle uninstalls the app and wipes the device's data.
- Generation ≈ 41 min, full benchmarks ≈ 63 min on the A56.
- Plans bottom bar is exitAlways: journeys must scroll Plans up to reveal a tab.
