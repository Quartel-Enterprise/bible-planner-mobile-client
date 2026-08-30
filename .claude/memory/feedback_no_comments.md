---
name: feedback_no_comments
description: "Don't add comments to production Kotlin source (inline //, block /* */, or KDoc). Comments ARE allowed in gradle scripts, the version catalog, and tests (Given/When/Then)."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 2ed50f6d-f9f7-42af-a4e2-df49db10c8a9
---

Do not add comments to production Kotlin source code — not inline `//`, not block `/* */`, and not KDoc `/** */` on declarations. The user deleted KDoc comments added during a task and stated the rule as "nunca adicione comentários".

**Exceptions where comments ARE allowed** (the user clarified "comentários em gradle, catalog e test são permitidos"):
- Gradle build scripts (`*.gradle.kts`) — e.g. section labels like `// Koin`.
- The version catalog (`gradle/libs.versions.toml`) — e.g. section headers like `# Firebase`.
- Test code — including `// Given` / `// When` / `// Then` markers.

**Why:** In production code the user considers added comments noise and wants the code to stand on its own; build config, the catalog, and tests use comments as helpful structure.

**How to apply:** In production Kotlin, rely on clear names instead of comments and don't narrate code; leave pre-existing comments in untouched code alone.

**Heavily documented neighbours are not an exception.** Parts of this repo (e.g. `feature/read`) are full of KDoc shipped after this rule was stated, so matching the surrounding comment density is the wrong instinct here — the user confirmed on 2026-08-30 (PR #384) that new code stays comment-free even when every sibling file has KDoc. Editing an existing KDoc that a signature change would otherwise make stale is fine; adding a new one is not. In gradle/catalog/test files, comments are fine — follow the file's existing convention. If documentation of production code seems genuinely necessary, ask first. Related: [[feedback_kdoc_simple_name_links]].
