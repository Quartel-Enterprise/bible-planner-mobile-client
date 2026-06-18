---
name: feedback_worktree_remote_only
description: "After finishing a worktree task and pushing, delete the local worktree and keep only the remote branch."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 2ed50f6d-f9f7-42af-a4e2-df49db10c8a9
---

When a task is done in a git worktree, push the branch to the remote, then remove the local worktree (ExitWorktree with action "remove"). Keep only the remote branch — do not leave the local worktree around.

**Why:** The user stated "delete a worktree, mantenha somente a branch remota a partir de agora" — they don't want stale local worktrees accumulating; the remote branch (and its PR) is the source of truth.

**How to apply:** Finish work → push → delete the worktree. Before any `--force`/`--force-with-lease` push, fetch and inspect the remote first: the user may have updated the PR branch (e.g. GitHub "Update branch" merge commits) that a blind force-push would clobber. Removing the local worktree deletes the local branch only; the remote branch and PR are unaffected.
