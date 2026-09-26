---
name: start-task
description: "Start a new task branched fresh from origin/main, either in an isolated git worktree or in-place in the current checkout."
---

# Start Task

This skill starts a new task from a fresh `origin/main`, either in an isolated git worktree under
`.claude/worktrees/` or in-place in the current checkout. A worktree keeps the task fully separate
from the main checkout and any other in-progress task, at the cost of an extra setup step (copying
git-ignored local config into the new worktree). In-place is faster to start but ties up the
current checkout for this one task.

Either way, the task also gets an emulator of its own, `BiblePlanner_<short_description>`, so any
device work it does never touches an emulator the user or another session is using.

## Step-by-step workflow

### 1. Determine how to start

Check whether the user's request already specifies a mode (e.g. "cria numa worktree", "começa
in-place", "direto no checkout atual"). If it does, use that mode and skip straight to step 2.

Otherwise, ask the user directly: "Quer que eu crie isso numa worktree isolada, ou direto in-place
no checkout atual?" Do not assume a default — wait for their answer.

### 2. Determine the branch type and short description

Pick the `<type>` prefix using the table in [`branch-types.md`](../branch-types.md) (shared with
`create-pr`).

If the user's request makes the type and a short kebab-case description obvious, use them directly.
Otherwise ask the user.

### 3. Sync with origin

```bash
git remote update origin
```

### 4. Create the branch

#### Worktree mode

Check for collisions first:

```bash
git worktree list
git branch --list <type>/<short-description>
ls .claude/worktrees/<short-description>
```

If the branch already exists, the worktree path is already registered, or the directory already
exists on disk (even unregistered — e.g. a leftover from a worktree removed without
`git worktree remove`), stop and tell the user instead of overwriting anything.

```bash
git worktree add -b <type>/<short-description> .claude/worktrees/<short-description> origin/main
```

This creates the branch and checks it out in the new worktree in one step, based on the latest
`origin/main` fetched in step 3 — not on whatever the main checkout currently has checked out.

Then copy the git-ignored local config into the new worktree, since a fresh worktree only contains
tracked files. Copy each one that exists in the main checkout:

```bash
WT=.claude/worktrees/<short-description>
cp local.properties "$WT/local.properties"
cp androidApp/google-services.json "$WT/androidApp/google-services.json"
cp iosApp/iosApp/GoogleService-Info.plist "$WT/iosApp/iosApp/GoogleService-Info.plist"
mkdir -p "$WT/.claude" && cp .claude/settings.local.json "$WT/.claude/settings.local.json"
```

- `local.properties` has `sdk.dir` and the local secrets; without it Gradle sync fails.
- `google-services.json` and `GoogleService-Info.plist` are required by the Android and iOS builds.
- `.claude/settings.local.json` gives the worktree's Claude Code session the same permission
  settings instead of re-prompting for everything.

If one of them doesn't exist in the main checkout, skip it and tell the user which one is missing.

Do **not** copy git-ignored build outputs or caches (`.gradle/`, `**/build/`, `.kotlin/`, `.idea/`,
`.claude/memory/`, `.claude/worktrees/`). Those are regenerated fresh per worktree; copying them can
carry over stale state.

#### In-place mode

Check the current checkout is clean and not already mid-task:

```bash
git status -s
git branch --show-current
```

If there are uncommitted changes, stop and ask the user how to proceed (commit, stash, or use
worktree mode instead) — don't discard or stash anything without asking. If the current branch
isn't `main` (or doesn't track `origin/main`), tell the user and confirm before switching away from
it, since that would leave whatever they're on now behind.

```bash
git checkout -b <type>/<short-description> origin/main
```

No file copying is needed here — the checkout already has all the local git-ignored config in
place.

### 5. Create the task's emulator

In both modes, create an emulator for this task only: `BiblePlanner_<short_description>`, a copy of
`Pixel_9`'s config. Follow the "Create" section of [`task-emulator.md`](../task-emulator.md), which
is shared with `finish-task`. It only writes two config files and does not boot the emulator.

Whenever the task needs an Android device (the Compose UI tests of `androidDeviceTest`, a look at
the running app), boot and use this emulator as described in that file's "Boot", "Run tests on it"
and "Run the app on it" sections. Never use the user's emulators or another task's.

### 6. Report the result

Worktree mode: tell the user the absolute path to the new worktree and the branch name, and that
it's ready to open (new Claude Code session, or Android Studio via "Open" on that path).

In-place mode: confirm the branch name and that the current checkout is now on it, ready to work.

In both modes, also name the task's emulator.

### 7. Start implementing immediately

If the user's request already described the task to do (bug to fix, feature to add, etc.) —
not just which branch/mode to use — don't stop after step 6 and wait for a "go ahead"/"pode
implementar". Continue straight into implementing that task in this same turn, right after the
short status report from step 6.

- Worktree mode: keep working in this same session — do not wait for a new session to be opened
  in the worktree. Point every file tool (Read/Edit/Write/Bash/etc.) at paths inside the new
  worktree directory (`.claude/worktrees/<short-description>/...`) instead of the main checkout,
  since tools work with any path regardless of the session's current working directory. A `cd`
  doesn't persist between Bash calls, so a bare `./gradlew` or `git` builds and inspects the main
  checkout: always use `./gradlew -p .claude/worktrees/<short-description>` and
  `git -C .claude/worktrees/<short-description>`.
- In-place mode: the current checkout is already on the new branch, so just continue normally.

Only stop and wait for the user if their request genuinely gave no task description (e.g. they
only said "start a task in a worktree" with no further detail) — in that case, ask what to
implement.

## Edge cases

- If `git worktree add` fails because the branch or path already exists, stop — don't force or
  pick a different name silently. Ask the user how they want to resolve it.
- Never run worktree mode from inside another worktree; it should always target
  `.claude/worktrees/<short-description>` relative to the main repo root.
- In in-place mode, if `git checkout -b` fails because the branch name already exists, stop and ask
  the user how they want to resolve it.
- If the task's emulator already exists (an AVD of that name, or its `.avd` directory), stop and
  ask. It may belong to a task that is still running, so don't reuse it or overwrite it.
