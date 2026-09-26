---
name: project-main-ruleset-required-check
description: main is guarded by a GitHub ruleset (not classic branch protection) requiring the check-translations status check; the classic protection API returns 404 and misleads.
metadata:
  node_type: memory
  type: project
  originSessionId: 8ebf5815-bb9d-4a03-b27f-849ae31512c5
  modified: 2026-09-26T01:23:08.060Z
---

`main` has no classic branch protection (`gh api .../branches/main/protection` → 404 "Branch not protected"), but a **ruleset** requires the `check-translations` status check (plus no deletion / no force-push). Inspect it with `gh api repos/{owner}/{repo}/rules/branches/main`.

**Why:** on 2026-09-25 an immediate `gh pr merge --squash` on #460 failed with "the base branch policy prohibits the merge" after the 404 had been read as "unprotected".

**How to apply:** before merging, wait for the check to register and pass (the create-pr skill's step 12 does this since #461/#462); never bypass with `--admin`. Related: [[feedback-worktree-remote-only]].
