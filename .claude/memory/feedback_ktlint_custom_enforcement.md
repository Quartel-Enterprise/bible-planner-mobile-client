---
name: feedback-ktlint-custom-enforcement
description: "user prefers repo-wide rename + real enforcement (custom ktlint rule) over docs-only convention changes, when asked to standardize"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: a53fbfca-ad2d-49bf-aa90-842363229fcc
---

When the user asks to standardize a naming/style convention that isn't already enforced by stock tooling, prefer offering (and if accepted, building) actual automated enforcement — e.g. a custom ktlint rule module — over settling for documentation-only guidance, and apply the rename across the whole repo immediately rather than only in the files touched this session.

**Why:** asked to standardize `PascalCase`/`SCREAMING_SNAKE_CASE` top-level private `val`s (e.g. `ButtonMinHeight`) to `camelCase` (`buttonMinHeight`). Confirmed via [[AskUserQuestion]] with scope options; user picked "repo inteiro agora" (not just touched files) and "sim, criar o módulo customizado" (build the custom ktlint rule) over the cheaper docs-only option, even though standard ktlint (`standard:property-naming`) explicitly permits `PascalCase` for this pattern and offers no config toggle to forbid it.

**How to apply:** when a similar ask comes up (standardize X across the codebase), default the `AskUserQuestion` options to include a "repo-wide now" and a "build real enforcement" choice as the recommended/first option rather than the minimal-diff option — this user tends to pick the thorough one. The resulting custom ktlint rule set lives at `tools/ktlint-custom-rules` (wired into every subproject's `ktlintRuleset` config from root `build.gradle.kts`) — read that module directly for current state rather than relying on this note for specifics.
