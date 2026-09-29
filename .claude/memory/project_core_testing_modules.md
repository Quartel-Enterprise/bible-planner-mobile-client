---
name: project-core-testing-modules
description: "Shared test fakes live in :core:<x>:testing modules (Sep/2026, branch refactor/core-api-impl-split); in-memory full-behavior fakes, excluded from Kover, graph-asserted test-only."
metadata:
  node_type: memory
  type: project
  originSessionId: e1afd675-c025-4281-8aff-d3aefe6f6b30
  modified: 2026-09-26T23:51:29.833Z
---

Outcome of the api/impl evaluation ([[project-core-api-impl-split-rejected]]): instead of splitting core, 8 `:testing` modules were added (books, plan, day_study, preferences:theme_selection, preferences:material_you, provider:data_store, provider:room, provider:supabase) holding 17 formerly duplicated fakes.

Conventions chosen:
- Shared fake = in-memory version of the real thing (public `MutableStateFlow` state, write lists like `favoriteUpdates`/`readStatusUpdates`, public `var` knobs like `statusError`/`eventsError`/`statusGate`), not `error("unused")` stubs.
- Its state never completes, so use cases that collect forever must run in `backgroundScope.launch {}` + `runCurrent()` (bit ObserveSelectedVersionUseCaseTest and ObserveThemeSyncUseCaseTest).
- One-off behavior: `object : Repo by FakeRepo(...) { override ... }`.
- Consumers add them under a `// Shared fakes` section at the end of `commonTest.dependencies`.
- Excluded from coverage via `!path.endsWith(":testing")` in build-logic Coverage.kt; graph rule `":(?!\\S*:testing ).* -X> :.*:testing"`.

**Why:** 17 fake classes were copied across up to 11 modules each.
**How to apply:** a new fake of a core interface needed by 2+ modules goes into that core module's `:testing` (create one if missing); single-module fakes stay private. Docs: docs/testing/fakes-and-coroutines.md#shared-fakes.
