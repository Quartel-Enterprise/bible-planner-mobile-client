---
name: finish-task
description: "Tear down a finished task once its PR has merged: remove its worktree, or switch back to main in-place, then delete the branch, delete the task's emulator and any left behind by dropped tasks, and sync origin."
---

# Finish Task

This skill cleans up after `start-task`, in either mode it can create a task in:

- **Worktree mode**: removes the worktree under `.claude/worktrees/` and deletes its branch.
- **In-place mode**: switches the main checkout back to `main` and deletes the branch that was
  checked out there.

In both modes it also deletes the emulator `start-task` created for the task, if there is one,
and the emulators of tasks that were dropped without this skill.

In both modes this only runs after confirming the branch's PR has actually merged. Running this
skill (or the user asking to clean up / finish the task) is itself the user's authorization to
remove the worktree, the branch and the task's emulator once that merge is verified — do not ask
for a second confirmation before step 4's cleanup. Still stop and ask before anything not covered
by that authorization: an unmerged branch (step 3), or discarding uncommitted changes (see Edge
cases).

All commands in this skill must run from the **main repo checkout**, not from inside a worktree
being removed — a worktree cannot remove itself, and `git branch -d` on a branch checked out
elsewhere will fail anyway.

## Step-by-step workflow

### 1. Identify the task, branch, and mode

```bash
git worktree list
git branch --show-current
```

- If the user named a task that matches a path under `.claude/worktrees/`, or the current working
  directory is inside one, that's **worktree mode**. Get its branch:
  ```bash
  git -C .claude/worktrees/<name> branch --show-current
  ```
- Otherwise, if the main checkout is currently on a branch other than `main`, that's **in-place
  mode** — the branch to clean up is that current branch.
- If it's ambiguous (e.g. more than one worktree exists and no task was named, or the main checkout
  is also mid-task), ask the user which task to finish.

### 2. Sync with origin

```bash
git remote update origin
```

### 3. Verify the branch is actually merged

Prefer checking the PR directly:

```bash
gh pr view <branch> --json state,mergedAt
```

If `gh` isn't available or there's no PR, fall back to:

```bash
git log origin/main..<branch> --oneline
```

An empty result means every commit on the branch is already in `origin/main`. PRs here are squash
merged, so a merged branch usually still lists its commits in this fallback: trust the PR state
whenever there is a PR.

**If the branch is not merged** (PR still open, no PR at all, or commits not in `origin/main`),
stop. Tell the user exactly what's unmerged and ask for explicit confirmation before proceeding —
never delete an unmerged task silently.

### 4. Clean up

State exactly what's about to happen — the worktree path and branch being removed, or "switch the
current checkout back to `main` and delete `<branch>`" for in-place — then proceed without waiting
for a further confirmation; the merge check in step 3 plus the user's request to finish the task
already authorize this.

**Worktree mode:**

```bash
git worktree remove .claude/worktrees/<name>
git branch -D <branch>
git remote update origin --prune
```

**In-place mode:**

```bash
git checkout main
git pull
git branch -D <branch>
git remote update origin --prune
```

A squash merge replaces the branch's commits with a single new one on `main`, so git never sees the
local branch as merged and `git branch -d` refuses it. Use `-D` only here, after step 3 confirmed
the PR merged.

**The task's emulator, in both modes:** after the branch is deleted, run the "Delete" section of
[`task-emulator.md`](../task-emulator.md). It deletes every `BiblePlanner_*` AVD whose branch no
longer exists: this task's, now that its branch is gone, and any left behind by a dropped task. It
never touches an AVD whose task still has a branch, nor one that isn't a `BiblePlanner_*`.

### 5. Verify cleanup

```bash
git worktree list
git branch --list <branch>
~/Library/Android/sdk/emulator/emulator -list-avds | grep '^BiblePlanner_'
```

None of them should show the removed task anymore, and every `BiblePlanner_*` AVD left should belong
to a branch that still exists. Report the result to the user, including which emulators were
deleted.

## Edge cases

- **Worktree mode:** if `git worktree remove` fails because of uncommitted changes (modified or
  untracked files) in the worktree, stop and show what's there. Only pass `--force` after the user
  explicitly confirms discarding those changes.
- **In-place mode:** if `git checkout main` fails because of uncommitted changes, stop and ask the
  user how to proceed (commit, stash, or discard) — don't discard anything without asking.
- If the local branch has commits that weren't in the merged PR (`git rev-parse <branch>` differs
  from `gh pr view <branch> --json headRefOid`), stop and show them with
  `git log <headRefOid>..<branch> --oneline` before deleting.
- If a worktree directory was already deleted from disk without `git worktree remove` (it will show
  as `prunable` in `git worktree list`), just run `git worktree prune` and then delete the branch as
  in step 4.
