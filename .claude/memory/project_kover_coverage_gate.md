---
name: project-kover-coverage-gate
description: "PR #464 (2026-09-26) — Kover 80% gate (merged + per new file) on jvm target in build-and-test CI; coverage 37%→95%; plan is 3 PRs (coverage, Compose UI tests, E2E)."
metadata:
  node_type: memory
  type: project
  originSessionId: 1cabacae-e3f3-4077-9b18-04560b6a7381
  modified: 2026-09-26T03:34:55.528Z
---

The build-and-test workflow (added in #464) runs `jvmTest :koverVerifyCi :verifyNewFilesCoverage`. The bar is 80% for both rules, and the user explicitly chose 80% from day one. Coverage now sits at 95.1%.

**Why:** the user felt the tests were useless because CI never ran them. The setup was ported from TuneScout and adapted to KMP: the Kover variant `ci` holds only the `jvm` target, and `configureCoverage` in build-logic skips `:ui:*`.

**How to apply:**
- New code needs tests at 80% per file. `verifyNewFilesCoverage` also flags classes that are only moved or renamed into a new file.
- The CI `build` job uses `scripts/write_placeholder_google_services.sh`. The real secrets live only in the `Production` environment, which needs a manual approval, so no PR job can read them.
- Supabase Functions SSE is tested through `SseMockEngine` in `feature/day_study` tests. It wraps MockEngine to add `SSECapability` and apply `ResponseAdapterAttributeKey`.
- Remaining plan (issues #468, #469): PR 2 adds Compose UI tests with `runComposeUiTest` in `commonTest`, which JetBrains recommends and which runs on JVM, iOS and `androidDeviceTest`. PR 3 adds E2E flows: plan→day→reading, books→chapter, verse note, preferences.
- Bugs found in #464, tracked as issues: #465 EndSessionUseCase swallows failures, #466 DayViewModel.onCleared loses debounced notes, #467 toMoneyFormat truncates.
- Related: [[project-billing-testing-boundary]], [[feedback-no-comments]].
