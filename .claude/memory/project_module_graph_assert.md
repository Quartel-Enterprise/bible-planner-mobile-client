---
name: project-module-graph-assert
description: "Module layering enforced by jraska modules-graph-assert (PR #447); Popcorn Guineapig rejected, maxHeight deliberately dropped."
metadata: 
  node_type: memory
  type: project
  originSessionId: 791d1ab0-e13c-4e88-bf21-198035612785
  modified: 2026-09-15T02:37:45.849Z
---

Gradle module layering is enforced by jraska `modules-graph-assert` (root `build.gradle.kts` `moduleGraphAssert` block, own `module-graph` workflow path-filtered to `*.gradle.kts`/build-logic/catalog/wrapper since Kotlin-only commits were ~55% of static-analysis runs and the job is ~1m50s of pure configuration; not a required check (only check-translations is), so path filtering is safe — revisit if it becomes required; PR #447 squash-merged 2026-09-14). Rules: only `:core:navigation`/`:core:provider:koin` (composition root) see `:feature:*`/`:ui:*`; nothing below sees them; `:ui:*` never sees `:feature:*`. Feature→feature (19 edges) and ui→core are allowed on purpose.

**Why:** CodandoTV Popcorn Guineapig was rejected after reading its source: it matches dependency targets by simple project name (collides on `books`/`profile`/`utils`, can't express "core -X> feature"), hardcodes KMP configurations (misses jvmMain/mobileMain/commonMainApi), and needs `--no-configuration-cache`. `maxHeight` (graph height was 13) was dropped because the depth comes largely from the intentional composition root and preference chains, so it would just get bumped.

**How to apply:** extend `restricted` with regexes over full module paths; `configurations` must list KMP production source sets explicitly. Verify a new rule fires by temporarily adding a violating restriction. Related: [[feedback-ktlint-custom-enforcement]].
