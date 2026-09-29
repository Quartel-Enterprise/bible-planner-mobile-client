---
name: project-core-api-impl-split-rejected
description: Splitting every core module into api/impl was measured (Sep/2026) and judged not worth it; shared test fakes modules are the part that pays off.
metadata:
  node_type: memory
  type: project
  originSessionId: e1afd675-c025-4281-8aff-d3aefe6f6b30
  modified: 2026-09-26T23:17:00.907Z
---

Evaluated 2026-09-26 (worktree refactor/core-api-impl-split, no PR): splitting all 34 `:core:*` modules into api/impl.

Measured on Kotlin 2.4.20, warm daemon, BooksRepositoryImpl as the probe:
- JVM, body-only change in impl → only `:core:books` compileKotlinJvm reruns (Kotlin ABI compile avoidance already does what the split promises).
- JVM, public ABI change → 21 dependents rerun but IC makes the whole build ~4s.
- iOS, body change → 28 klib compiles cascade, but `:shared:linkDebugFrameworkIosSimulatorArm64` takes ~39s with or without them (link dominates, it relinks on any change).
- Configuration without config cache ~2-3s for ~88 projects; the split adds ~30 projects.

Costs found: ~30 Pattern B concrete use cases (e.g. GetBooksFlowUseCase, HasCooldownElapsedUseCase) consumed directly by features would need interfaces; 3 feature DI modules construct core classes (feature/day, edit_plan_start_date); tiny modules (core/clear 72 LOC, json_reader 27 LOC) would double.

What does pay off: 17 test Fake names duplicated across modules (FakeBooksRepository x11, FakePreferencesDataStore x10, FakeBibleRepository x10, FakePlanRepository x8) → per-core `:testing` modules. Encapsulation goal is reachable with `internal` on the 58 public impl classes that aren't consumed cross-module.

**Why:** revisit only if a concrete trigger appears (impl pulling a heavy SDK that features must not see, or a cycle to break) — not as a blanket rule.
**How to apply:** if the user raises api/impl again, cite these numbers and suggest selective splits or `:testing` modules instead. Related: [[project-module-graph-assert]], [[project-feature-isolation]].
