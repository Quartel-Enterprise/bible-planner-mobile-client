# Branch types

Shared by `create-pr` and `start-task` — used to pick the `<type>` prefix for a branch, commit, or
PR title, and the label of the pull request. Keep this table in sync in one place instead of copying
it into each skill.

| Type | When to use | PR label |
|---|---|---|
| `fix` | Corrects a bug or unintended behavior | `bug` |
| `feature` | Adds new functionality visible to the user | `feature` |
| `enhancement` | Improves existing functionality (performance, UX, accessibility) | `enhancement` |
| `refactor` | Internal code restructuring with no user-facing change | `refactor` |
| `chore` | Build, CI, tooling or documentation only | *(none)* |

If the changes or the user's request clearly point to one type, use it without asking. If it's
genuinely ambiguous, ask the user.
