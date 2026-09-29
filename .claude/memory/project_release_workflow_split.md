---
name: project-release-workflow-split
description: "Release workflow split into retryable jobs (PR #490); the iOS release link is the dominant cost (~33 min on CI vs ~5 min locally); how to test workflow graphs with act"
metadata:
  node_type: memory
  type: project
  originSessionId: bd5dd9e3-a130-4797-a454-7e167e9a10e2
  modified: 2026-09-29T03:42:10.267Z
---

PR #490 (merged 2026-09-29, not yet exercised by a real release: a beta via
`./scripts/release/release-beta.sh` is the first real test) splits `release.yml` so "Re-run failed
jobs" repeats only what failed:
`ios-build` → `ios-upload` (binary only, skips when the build is already on App Store Connect) →
`ios-submit` (listing + review), with `ios-screenshots-render` → `ios-screenshots-upload` next to
the build. `store-screenshots.yml` is `render` → `upload`. Fastfile gained `ios submit` and a
`skip_render:true` option on both `upload_screenshots` lanes.

**Why:** 2.9.1's retry re-rendered every screenshot and then died re-uploading build 56
("bundle version must be higher"), so the release was finished by hand (#489).

**Measured, not in the repo:**

- The release's critical path is all iOS. 2.9.0: 76 min = 43 build + 22 screenshot render + 8
  upload/processing/submit. Rendering is ~95% of the screenshot step and needs nothing from the IPA.
- `linkReleaseFrameworkIosArm64` dominates `ios-build`, even with a warm Gradle cache and only 3
  modules recompiled. Why, and what was ruled out, is in [[project-ios-release-link-hardware]].
  "Build the signed IPA" grew from ~20 min (June) to ~41 min (September).
- xcodebuild buffers the Gradle output of the "Compile Kotlin Framework" phase, so the CI log
  cannot tell compile from link.
- The `Production` environment has only a branch policy, no required reviewers, although
  `docs/release-process.md` describes an approval gate.
- On `main` before #490, a failed `ios-build` left `ios-upload` skipped (not failed) and `finalize`
  would still have run. It never happened only because those runs were cancelled by hand.

**How to apply:**

- To test a workflow's job graph locally: generate a copy where each job keeps name/needs/if/
  outputs/continue-on-error and its steps become one stub that fails on demand, then run it in
  `act` with `-P ubuntu-latest=node:24-bookworm-slim -v` and grep "Skipping job" to tell skipped
  from never reached. act maps every job to Linux, so this checks conditions, not macOS steps.
- act 0.2.84 cannot speak `upload-artifact@v7`'s protocol (`unknown field "mime_type"`); use v4 of
  both artifact actions to test path layout. It also warns about CVE-2026-34041/34042: upgrade.
- The Bash tool runs zsh, which does not word-split unquoted variables: `$BASE` holding several
  `key=value` inputs reaches act as one argument. Put scenario scripts in a file run by `bash`.
- To test Fastfile lanes without credentials: `Fastlane.load_actions`, then redefine `run` on the
  action classes and `Spaceship::ConnectAPI::App.find` / `Build.all`, and drive
  `Fastlane::FastFile.new(path).runner.execute(lane, platform, options)`. fastlane still validates
  every option, so a misspelt one fails. Run it with Homebrew's ruby and fastlane's GEM_PATH.
- The `gh` token has no `workflow` scope. `gh pr merge` still merges a PR that changes a workflow
  when its branch is up to date with `main`, and refuses ("refusing to allow an OAuth App to create
  or update workflow") when the branch is behind and `main` changed the same workflow. Merge
  `origin/main` into the branch, push over SSH, then merge. No force-push, no `--admin`.
- Related: [[project-ci-gradle-cache-r8]], [[project-app-store-screenshots-ios-sim]],
  [[project-ios-dsym-unrecoverable]].
