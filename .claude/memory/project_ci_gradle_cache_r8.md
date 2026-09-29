---
name: project-ci-gradle-cache-r8
description: "PRs #470/#471 (2026-09-26) — CI Gradle cache ported from TuneScout (per-job strict match, GRADLE_ENCRYPTION_KEY, parallel) cut build-and-test ~11m→~4m20s; R8 was never cacheable because Crashlytics injects a random mapping id + commit SHA into resources."
metadata:
  node_type: memory
  type: project
  originSessionId: 2a5be2b1-dc30-4827-b981-ceb76fdf4528
  modified: 2026-09-26T04:11:37.554Z
---

build-and-test went from ~11m (#464: unit-tests 9m48s with 1227/1227 tasks executed, build 11m11s) to ~4m20s (#470: unit-tests 1m14s, build 4m19s, module-graph 1m55s→45s). Most of that gain came from main finally having the job's own cache entry. Before, unit-tests restored the entry ktlint had saved on main, because setup-gradle falls back to the newest entry of any job. The cache setup (strict match on PRs, read-only ktlint/module-graph, `build` writes on PRs, cleanup-pr-caches) is described in docs/ci.md.

**Why:** the user wanted CI times down and pointed at TuneScout (`/Users/pierrevieira/StudioProjects/musicAi/TuneScout`), which already had these fixes. It is the reference for further CI work, see [[project-tunescout-ktlint-port]].

**How to apply:**
- The `GRADLE_ENCRYPTION_KEY` repo secret was created on 2026-09-26 (random key, value never seen). The configuration cache only pays off from the second push of a PR on. The first run after any build-script change always shows "no cached configuration".
- R8 is ~2/3 of the `build` job. The Crashlytics plugin 3.x writes a random mapping file id per build (unless `mappingFileUploadEnabled=false` → all-zeros id) and AGP/Crashlytics write the commit SHA (vcsInfo) into res. Both are R8 inputs, so R8 never came from the cache. Since #471 both are enabled only when `ANDROID_KEYSTORE_PATH` is set (release workflow). Local and CI release builds no longer upload mappings.
- R8 still reruns on any app-code change (whole-program). Further cuts would mean dropping the R8 check from PRs or bigger runners; neither was recommended.
- Timing evidence comes from job logs: `gh run view <id> --log` plus grep for "actionable tasks", "Restored  Key" ("partial match" = fallback) and "> Task" timestamps (plain console on CI).
- Related: [[project-kover-coverage-gate]], [[project-crashlytics-integration]].
