---
name: project-e2e-lazy-scroll-fallback
description: E2E awaitNode scrolls lazy lists to missing nodes only after 2s and only in the topmost root; why both limits exist
metadata:
  node_type: memory
  type: project
  originSessionId: caf71051-01e0-4f0a-9dd4-d9577064aa11
  modified: 2026-09-27T04:30:45.823Z
---

PR #485 (2026-09-27): E2E harness `awaitNode` (shared commonTest E2eInteractions.kt) falls back to `performScrollToNode` on lazy lists when a node is missing, because lazy items below the fold are never composed (broke wide-window PreferencesFlowUiTest after #484 added the Annotations card to Profile).

**Why the limits:** scrolling immediately hit the screen a tab-switch transition was leaving and the transition never finished; scrolling the screen behind an open sheet triggered `exitAlwaysScrollBehavior` in MainTabScaffold, hiding the portrait bottom bar so the next tab click landed off-screen. Semantic ScrollBy goes through nested scroll, so any harness scroll can hide the bar.

**How to apply:** when touching E2E waits/scrolls, keep the 2s settle + topmost-root filter; the `ui-tests` workflow is not a required check (only check-translations), so wait for all checks before merging. Related: [[project-compose-ui-tests-setup]], [[project-main-ruleset-required-check]].
