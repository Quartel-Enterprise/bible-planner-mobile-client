---
name: project-worktree-gradle-cwd-trap
description: "In background Bash commands `cd` does not persist, so `./gradlew` runs the main repo instead of the worktree — always pass `gradlew -p <worktree>` and `git -C <worktree>`."
metadata: 
  node_type: memory
  type: project
  originSessionId: 9b025979-d9e7-4c92-9bc5-9d30ec7898f8
  modified: 2026-07-31T22:21:49.254Z
---

When running builds for a git worktree under `.claude/worktrees/`, always use explicit paths:
`"$W/gradlew" -p "$W" :desktopApp:run` and `git -C "$W" ...`.

**Why:** the session cwd stays at the main repo (`/Users/pierrevieira/StudioProjects/bible-planner/bible-planner-mobile-client`), and a `cd` inside a `run_in_background` Bash command does not carry over — the tool result says so explicitly. A bare `./gradlew :desktopApp:run` therefore builds and launches the **main repo's** checkout (often a different branch entirely), so worktree edits appear to have no effect. On 2026-07-31 this silently invalidated two rounds of UI testing: the app under test never contained the change.

**How to apply:** before concluding "the change didn't work", confirm the build actually compiled the worktree — grep the Gradle log for the module's compile task actually executing (not `UP-TO-DATE`), and check the branch printed from `git -C "$W" branch --show-current`. See [[feedback-worktree-remote-only]] for the branch lifecycle in worktrees.
