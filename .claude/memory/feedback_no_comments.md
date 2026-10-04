---
name: feedback_no_comments
description: "Production Kotlin comments only say WHY, as a one-line `// Why:` or a multi-line `/*\n * Why: ...\n */` block (bug workarounds with issue ids, invariants, deliberate odd choices, external constraints); never narration, KDoc or other /* */. Tests, gradle scripts, build-logic and the catalog may comment freely."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 2ed50f6d-f9f7-42af-a4e2-df49db10c8a9
---

Production Kotlin never says **what** it does in comments: no narration, no section labels, no KDoc `/** */`, no block `/* */`. The user first stated this as "nunca adicione comentários" after deleting KDoc added during a task.

In Oct/2026 (#515/#517) the user refined it: a blanket ban is "too much" and dangerous, because some comments hold the **why** (a platform-bug workaround, a sync invariant like last-write-wins, an external constraint such as the Supabase redirect allowlist) and deleting them invites a "simplification" that brings the bug back. Those stay, written as `// Why: ...` when they fit on one line. Since Oct/2026 (2026-10-04) a multi-line reason is a block comment — `/*` alone, ` * ` per line, ` */` alone — never a `// Why:` continued on `//` lines (the user asked for multi-line comments to follow the multi-line comment form; 246 of 307 existed as `//` runs and were converted by `ktlint.sh --format`). When the why is a behaviour, a test named after the rule guarantees it better; the comment then stays to one line.

**Exceptions where any comment is fine** (the user: "comentários em gradle, catalog e test são permitidos"):
- Gradle build scripts (`*.kts`) and `build-logic` — e.g. section labels like `// Koin`.
- The version catalog (`gradle/libs.versions.toml`) — e.g. `# Firebase`.
- Test code and the `:testing` fake modules — including `// Given` / `// When` / `// Then`.

**How to apply:** rely on names for the what. Add a `// Why:` only when a reader could not recover the reason from the code and losing it could cause a bug; keep issue ids/links so the comment can go when the upstream fix ships. Related: [[feedback_kdoc_simple_name_links]].

**Enforced** by the ktlint rule `bible-planner-style:comments-say-why` (form only: a one-line `// Why:` and a starred multi-line `Why:` block pass, everything else fails; the Why shape is autocorrected, other comments are not). Whether a `// Why:` really states a reason is for the review. `.editorconfig` disables the rule for test source sets, `:testing` modules, `*.kts` and `build-logic`.
