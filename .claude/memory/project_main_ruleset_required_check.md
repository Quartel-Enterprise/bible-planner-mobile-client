---
name: project-main-ruleset-required-check
description: main is guarded by a GitHub ruleset (not classic branch protection) requiring check-translations + resolved review conversations (since 2026-10-04); the classic protection API returns 404 and misleads.
metadata:
  node_type: memory
  type: project
  originSessionId: 8ebf5815-bb9d-4a03-b27f-849ae31512c5
  modified: 2026-10-04T04:00:00.000Z
---

`main` has no classic branch protection (`gh api .../branches/main/protection` → 404 "Branch not protected"), but a **ruleset** requires the `check-translations` status check (plus no deletion / no force-push). Inspect it with `gh api repos/{owner}/{repo}/rules/branches/main`.

**Why:** on 2026-09-25 an immediate `gh pr merge --squash` on #460 failed with "the base branch policy prohibits the merge" after the 404 had been read as "unprotected".

**How to apply:** before merging, wait for the check to register and pass (the create-pr skill's step 12 does this since #461/#462); never bypass with `--admin`. Related: [[feedback-worktree-remote-only]].

Since 2026-10-04 (ruleset id 10422461 "Main rules") it also has a `pull_request` rule: 0 approvals, `required_review_thread_resolution: true`, no bypass. An open Claude review comment blocks every merge (manual and merge-when-green, which now comments + drops its label instead). The API silently defaults `require_extra_approval_for_unattributed_changes` to true when the rule is created; it was set back to false, because a solo maintainer can't approve their own PRs. Re-check it after any ruleset PUT.
