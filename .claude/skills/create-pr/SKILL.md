---
name: create-pr
description: "Create a pull request following all project conventions: Conventional Commits title, description template, assignee, and labels."
---

# Create PR

This skill formats the code, commits any uncommitted changes, pushes the branch, and opens a pull
request with a clear title and description inferred from the actual changes.

## Step-by-step workflow

### 1. Gather context

Run these in parallel:
- `git status -s` to check for uncommitted changes
- `git diff --stat` to see unstaged changes
- `git log --oneline -1` to get the latest commit
- `git rev-parse --abbrev-ref HEAD` to get the current branch name
- `git log --oneline develop..HEAD` to see all commits on this branch
- `git diff develop...HEAD --stat` to see total changes vs develop

### 2. Run ktlint formatter

Run from the project root:

```bash
./gradlew ktlintFormat
```

If the command exits with a non-zero code, stop and notify the user:
> "ktlint found issues it couldn't fix automatically. Please review and fix the reported errors, then try again."

Do not proceed until the formatter succeeds.

### 3. Check the current branch

```bash
git branch --show-current
```

Also check the upstream tracking branch:

```bash
git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null
```

**If the current branch is `develop`**, or the upstream tracking branch is `origin/develop`, the
user is working directly on develop — a new branch must be created before committing. Continue to
step 4 to determine the prefix, then create the branch in step 5.

**If the current branch is already a feature/fix/enhancement/refactor branch**, skip branch
creation and go directly to step 5.

### 4. Infer the change type

Inspect the uncommitted changes and any commits ahead of origin/develop to understand what kind of
change this is:

```bash
git remote update
git diff HEAD
git log origin/develop..HEAD --oneline
```

Based on the changes, choose the most appropriate type:

| Type | When to use |
|---|---|
| `fix` | Corrects a bug or unintended behavior |
| `feature` | Adds new functionality visible to the user |
| `enhancement` | Improves existing functionality (performance, UX, accessibility) |
| `refactor` | Internal code restructuring with no user-facing change |

If the changes clearly point to one type, use it without asking.
If it's genuinely ambiguous, ask the user:
> "What type of change is this?"
> - Bug fix
> - New feature
> - Improvement to something existing
> - Internal code cleanup (no visible change)

### 5. Create a new branch (if needed)

If the user is on `develop` (determined in step 3), create and switch to a new branch. Derive the
branch name from the changes — keep it short, lowercase, hyphen-separated:

```bash
git checkout -b <type>/<short-description>
```

Examples:
- `fix/whitespace-below-search-bar`
- `feature/offline-bible-reading`
- `enhancement/books-screen-filters`
- `refactor/release-notes-viewmodel`

### 6. Update release notes (if user-facing)

Before committing, decide whether the change is user-facing:

- **Run the release-notes-updater skill** if the type is `fix`, `feature`, or `enhancement` AND
  the change has a visible impact on the user (UI, behavior, new screen, crash fix, etc.)
- **Skip it** if the type is `refactor`, or if the change is purely internal with no perceptible
  effect on the user (e.g. dependency update, build config, architecture cleanup, test additions)

When in doubt, lean toward running the skill — it's better to have an extra release note than
to miss a user-facing change.

Invoke the skill by following the instructions in `docs/skills/release-notes-updater.md`. The
changes inferred in step 3 should be enough context — pass them along so the skill doesn't need
to re-run the git commands.

### 7. Commit all uncommitted changes

If the release-notes-updater skill was run in step 6, show the user what was written and ask:
> "The release notes have been updated. Want me to commit now?"

Wait for confirmation before proceeding. If the user wants to adjust the notes first, let them — then ask again.

Stage everything (including any release notes updates) and create a single commit:

```bash
git add -A
git commit -m "<type>: <short description>"
```

The commit message must:
- Start with the type prefix (`fix:`, `feature:`, `enhancement:`, `refactor:`)
- Be concise and in the imperative form (e.g. "fix: remove extra whitespace below search bar")
- Not exceed 72 characters

If there are no uncommitted changes (the user already committed everything), skip this step.

### 8. Push the branch

```bash
git push -u origin HEAD
```

### 9. Create the pull request

First, check if the GitHub CLI is available:

```bash
gh --version
```

**If `gh` is not installed**, suggest installing it and ask before proceeding:
> "GitHub CLI (`gh`) is not installed. Would you like me to install it now?"

If the user agrees, install it:
```bash
# macOS
brew install gh
```

After installation, authenticate if needed:
```bash
gh auth status || gh auth login
```

Once `gh` is available, create the PR:

```bash
gh pr create \
  --base develop \
  --title "<type>: <short description>" \
  --body "<description>" \
  --assignee @me \
  --label "<label>"
```

Choose the label based on the change type:

| Type | Label |
|---|---|
| `fix` | `bug` |
| `feature` | `feature` |
| `enhancement` | `enhancement` |
| `refactor` | `refactor` |

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

## Edge cases

- If ktlint fails, stop immediately — do not commit or push
- If the user is already on a correctly prefixed branch, skip branch creation
- If there is nothing to commit and the branch is already pushed, go directly to PR creation
- If the branch already has an open PR, notify the user instead of creating a duplicate
- Always target `develop` as the base branch for the PR
- Release notes updates (step 6) are included in the same commit as the rest of the changes — do not create a separate commit for them
