---
name: project-ios-release-link-hardware
description: "iOS release link time is set by the runner's hardware (M1 7 GB: 26 min, M2 Pro 14 GB: 10 min); macos-latest deals either; JVM heap/GC tuning ruled out (PR #491)"
metadata:
  node_type: memory
  type: project
  originSessionId: bd5dd9e3-a130-4797-a454-7e167e9a10e2
  modified: 2026-09-29T02:31:38.036Z
---

`:shared:linkReleaseFrameworkIosArm64` is most of the release's `ios-build` job. Measured on
2026-09-28 with a temporary workflow (runs 36503159145, 36508097182, 36511460647), written up in
`docs/ci.md` "iOS release link" by PR #491.

- The link needs ~13 GB in the Gradle daemon plus 3–6 GB in the linker child (LTO, all modules
  together). Link time: M1 3 cores/7 GB 26 min with 4.4 GB of swap; M2 Pro 5 cores/14 GB 10 min,
  no swap; `macos-15-intel` 4 cores/14 GB 22 min; local M4 Max 5–6 min.
- `macos-latest` deals either the M1 or the M2 Pro (7 of 21 jobs got the M2 Pro). The pinned labels
  `macos-26`/`macos-15`/`macos-14` gave the M1 in 25 of 25. Never pin `ios-build` to a version
  label: it would lose the chance of the faster machine.
- Ruled out: heap 6 GB (16% slower on M1, 43% on Intel), heap 5 GB and 4 GB (OutOfMemoryError),
  heap 12 GB (no change locally), ParallelGC at 6 GB (thrashes for over an hour), G1 periodic GC
  with low heap free ratios (lowers the local peak to 8.7 GB, changes nothing on the runners).
- CI compile of the 79 modules is under 5 minutes cold. Compile is not the problem.

**Why:** the first guess, that the 8 GB heap on a 7 GB runner was the cause, is half right: the
runner does swap, but no JVM setting relieves it, because the compiler's live heap alone is over
5 GB.

**How to apply:**

- What would shorten the link, all user decisions, none applied: `macos-latest-xlarge` (the M2 Pro
  every time, billed even on a public repo, needs a plan with larger runners); the release binary
  cache (`kotlin.native.binary.enableReleaseBinaryCache` + `kotlin.internal.native.
  enableReleaseBinaryCache`): 10 min on the M1, 37 s locally when warm, but no cross-module LTO and
  a static framework of 444 MB instead of 287 MB; retrying `ios-build` until it lands on 14 GB.
- To change the daemon's JVM args for one CI job, append `org.gradle.jvmargs=...` to
  `~/.gradle/gradle.properties`. `-Dorg.gradle.jvmargs=` on the command line is not honoured.
- To tell a daemon apart locally, give it a unique `-Xmx`: system properties passed in jvmargs do
  not show on its command line, and do not force a new daemon either.
- Never run `gradlew --stop` on the user's machine: it stops every daemon of that Gradle version,
  Android Studio's included. Kill only the PID of the daemon the measurement started. Killing a
  Gradle client mid-build also takes down the daemon it was using.
- Release Gradle variants can be built on CI without secrets: BuildKonfig bakes empty keys without
  `local.properties`. The `Production` environment only accepts `main`, so a branch cannot use it.
- `gh workflow run` cannot dispatch a workflow that is not on the default branch: a temporary
  workflow has to trigger on `push` to its branch, filtered by `paths` to its own file.
- After `git rm`, `git add <that path>` fails and breaks an `&&` chain: check the commit landed
  before `gh pr create` (PR #491 was first opened without its final commit).
- Related: [[project-release-workflow-split]], [[project-ci-gradle-cache-r8]].
