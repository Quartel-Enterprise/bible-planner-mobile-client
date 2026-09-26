---
name: create-pr
description: "Create a pull request following all project conventions: ktlint formatting, unit tests, release notes, Conventional Commits title, description template, assignee, and labels."
---

# Create PR

This skill formats the code, runs the tests, commits any uncommitted changes, pushes the branch, and
opens a pull request with a clear title and description inferred from the actual changes.

## Step-by-step workflow

### 1. Gather context

Run these in parallel:
- `git status -s` to check for uncommitted changes
- `git diff --stat` to see unstaged changes
- `git log --oneline -1` to get the latest commit
- `git rev-parse --abbrev-ref HEAD` to get the current branch name
- `git log --oneline main..HEAD` to see all commits on this branch
- `git diff main...HEAD --stat` to see total changes vs main

### 2. Run ktlint formatter (if needed)

Check whether any `.kt` or `.kts` files are among the changed files gathered in step 1. If none are
present, skip this step entirely.

If there are Kotlin files changed, run from the project root:

```bash
./scripts/ktlint.sh --format
```

This is the ktlint CLI, which is exactly what the `static-analysis` workflow runs in CI. Do **not**
use `./gradlew ktlintFormat` — the Gradle plugin path is far slower and is no longer what CI checks.

If the command exits with a non-zero code, stop and notify the user:
> "ktlint found issues it couldn't fix automatically. Please review and fix the reported errors, then try again."

Do not proceed until the formatter succeeds.

Formatting rewrites files in place, so re-run `git status -s` afterwards if you need an up-to-date
list of changed files for the steps below.

### 3. Check the current branch

```bash
git branch --show-current
```

Also check the upstream tracking branch:

```bash
git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null
```

**If the current branch is `main`**, or the upstream tracking branch is `origin/main`, stop and ask
whether the user wants to use the `start-task` skill instead — it's the preferred way to start new
work and will ask whether to use a worktree or work in-place. If they'd rather branch directly here
without going through that flow, continue to step 4 to determine the prefix, then create the branch
in step 5.

**If the current branch is already a feature/fix/enhancement/refactor/chore branch**, take the type
from its prefix and go directly to step 6.

### 4. Infer the change type

Inspect the uncommitted changes and any commits ahead of origin/main:

```bash
git remote update
git diff HEAD
git log origin/main..HEAD --oneline
```

Pick the `<type>` prefix using the table in [`branch-types.md`](../branch-types.md).

### 5. Create a new branch (if needed)

Derive the branch name from the changes — keep it short, lowercase, hyphen-separated:

```bash
git checkout -b <type>/<short-description>
```

Examples: `fix/whitespace-below-search-bar`, `feature/offline-bible-reading`,
`enhancement/books-screen-filters`, `refactor/release-notes-viewmodel`, `chore/ci-module-graph`.

### 6. Update release notes (if user-facing)

Before committing, decide whether the change is user-facing:

- **Run the `release-notes-updater` skill** if the type is `fix`, `feature`, or `enhancement` AND
  the change has a visible impact on the user (UI, behavior, new screen, crash fix, etc.)
- **Skip it** if the type is `refactor` or `chore`, or if the change is purely internal with no
  perceptible effect on the user (e.g. dependency update, build config, architecture cleanup, test
  additions)

When in doubt, lean toward running the skill — it's better to have an extra release note than
to miss a user-facing change. The changes inferred in steps 1 and 4 should be enough context — pass
them along so the skill doesn't need to re-run the git commands.

### 7. Verify analytics catalog (feature/enhancement)

Skip this for `fix`, `refactor` and `chore`. For `feature` and `enhancement`, check whether the diff
adds or changes user actions that should be tracked — i.e. it touches
`feature/**/presentation/model/*UiEvent.kt` (new or changed `UiEvent` cases) or other feature
code introducing user-facing interactions.

The compiler already guarantees every `UiEvent` declares an `analytics` decision, so the gap here
is the part the compiler can't see: the **event catalog and constants**. If the diff added
trackable actions, confirm it also updated:

- `docs/analytics/events/<name>.md` (a new event file) and the index table in
  `docs/analytics/README.md`, and
- `AnalyticsEventNames.kt` / `AnalyticsParams.kt` constants for any new event.

If a user-facing feature added trackable actions but none of the above changed, warn the user and
offer to run the `add-analytics-event` skill before committing:

> "This feature adds user actions but the analytics catalog wasn't updated. Want me to run
> add-analytics-event to add the events, or are these deliberately not tracked?"

Do not block on it — if the user confirms the actions are deliberately `NotTracked`, continue.

### 8. Run the unit tests (if needed)

If every file the branch changes is Markdown (`.md`), as gathered in step 1 (commits ahead of
`main` and uncommitted changes alike), skip this step. No test can read those files, and the run
takes minutes.

Otherwise, run:

```bash
./gradlew jvmTest :koverVerifyCi :verifyNewFilesCoverage --continue
```

It runs every module's `commonTest` and `jvmTest` on the JVM target and the two coverage rules the
`build-and-test` workflow enforces: 80% of the lines for the merged report and for each file the
branch adds (see [docs/code-quality.md](../../../docs/code-quality.md#test-coverage)). If tests fail
or a rule fails, stop and report it: write the missing tests rather than pushing a red branch.

### 9. Commit all uncommitted changes

If the `release-notes-updater` skill was run in step 6, show the user what was written and ask:
> "The release notes have been updated. Want me to commit now?"

Wait for confirmation before proceeding. If the user wants to adjust the notes first, let them —
then ask again.

Stage everything (including any release notes updates) and create a single commit:

```bash
git add -A
git commit -m "<type>: <short description>"
```

The commit message must:
- Start with the type prefix (`fix:`, `feature:`, `enhancement:`, `refactor:`, `chore:`)
- Be concise and in the imperative form (e.g. "fix: remove extra whitespace below search bar")
- Not exceed 72 characters

If there are no uncommitted changes (the user already committed everything), skip this step.

### 10. Push the branch

```bash
git push -u origin HEAD
```

### 11. Create the pull request

```bash
gh pr create \
  --base main \
  --title "<type>: <short description>" \
  --body "<description>" \
  --assignee @me \
  --label "<label>"
```

Take the label from the table in [`branch-types.md`](../branch-types.md). `chore` has no label:
drop the `--label` flag for it.

If `gh` is not installed, ask the user before installing it (`brew install gh`, then
`gh auth status || gh auth login`).

**Title:** Same format as the commit message — type prefix + short imperative description.

**Description:** Write a clear summary of what changed and why, at a medium level of detail:
- Describe the problem being solved or the feature being added
- Mention the affected screens or areas of the app
- Explain the approach taken if it is not obvious
- Do NOT list every file changed or describe code line by line
- Do NOT include purely internal implementation details (e.g. which class was refactored)
- Keep it to 3–8 sentences or a short bullet list

## Example

Given uncommitted changes that remove a `navigationBarsPadding()` modifier from a top bar:

- **Branch:** `fix/extra-whitespace-below-search-bar`
- **Commit:** `fix: remove extra whitespace below search bar on books screen`
- **PR title:** `fix: remove extra whitespace below search bar on books screen`
- **PR description:**
  > The books screen had extra whitespace appearing below the search bar due to `navigationBarsPadding()` being applied to the top bar instead of the screen content. This modifier adds padding matching the system navigation bar height, which caused the top bar surface to grow downward unnecessarily. Removed the modifier from the top bar to fix the layout.

### 12. Squash and merge (optional)

If the user's request already asked for the merge (e.g. "abre o PR e mergeia"), skip the question
and merge. Otherwise, after the PR is created, use the `AskUserQuestion` tool to ask whether to
squash merge now:

- Yes — squash merge and clean up the task
- No — end the workflow here

If yes, wait for the required checks, then merge:

```bash
for _ in $(seq 20); do gh pr checks --required 2>/dev/null | grep -q . && break; sleep 3; done
gh pr checks --watch --required --interval 5
gh pr merge --squash
```

A ruleset on `main` requires the `check-translations` status check, so `gh pr merge` refuses to
merge until it passes ("the base branch policy prohibits the merge"). It takes a few seconds. Right
after the push, GitHub hasn't registered the check yet and `gh pr checks` fails with "no checks
reported", so the loop first waits up to a minute for it to show up. If it never does, check the
workflow runs with `gh run list --branch <branch>` and tell the user instead of merging. Never
bypass it with `--admin`. If a required check fails, report it and stop. If the merge fails for
another reason (e.g. a conflict with `main`), notify the user and stop.

Once the squash merge succeeds, immediately run the `finish-task` skill in the same turn — don't
wait for the user to ask for it. Asking for the squash merge is also the request to clean up the
task. `finish-task` handles both worktree and in-place tasks and checks the merge before removing
the branch (and the worktree, if there is one), so don't duplicate its logic here.

## Edge cases

- If `./scripts/ktlint.sh --format` fails, stop immediately — do not commit or push. The remaining
  errors are ones ktlint cannot autocorrect (e.g. custom `bible-planner-style:*` rules), so they
  must be fixed by hand
- If there is nothing to commit and the branch is already pushed, go directly to PR creation
- If the branch already has an open PR, notify the user instead of creating a duplicate
- Always target `main` as the base branch for the PR
- Release notes updates (step 6) are included in the same commit as the rest of the changes — do
  not create a separate commit for them
