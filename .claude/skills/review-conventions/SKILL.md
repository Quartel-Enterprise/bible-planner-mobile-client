---
name: review-conventions
description: "Review the branch's changes against the project's architecture, testing and analytics docs, the product invariants, and the intent of the pull request. Reports findings only, never edits files. Run by create-pr before the tests; pass the pull request description as the argument."
argument-hint: "[pull request description]"
context: fork
agent: general-purpose
background: false
disallowed-tools: Edit Write NotebookEdit
---

# Review conventions

You are reviewing someone else's change. You don't see the conversation that produced it: judge it
from the diff, the repository and the intent below, nothing else. Don't edit any file. Your output is
a list of findings for the author to decide on.

## The intent of the change

The author wrote this as the pull request description, before the review:

<intent>
$ARGUMENTS
</intent>

If the block above is empty, say so in your report and review only against the docs and the
invariants: without an intent you can't tell whether the change does what it should.

## 1. Collect the change

The change is everything the branch adds on top of `main`, committed or not:

```bash
git fetch origin main
base=$(git merge-base origin/main HEAD)
git diff "$base" --stat
git diff "$base"
git ls-files --others --exclude-standard
```

Untracked files are part of the change too: read each one in full. When a hunk isn't enough to judge
it, read the whole file.

## 2. Read the docs the change touches

The rules live in the docs that [docs/ai_agents.md](../../../docs/ai_agents.md) links. They are not
loaded into your context by default, so read the ones that apply to the files in the diff, always in
full:

| The diff touches | Read |
|---|---|
| Any Kotlin file | `docs/architecture/code-style.md`, `docs/architecture/dry.md` |
| A new or moved module, a `build.gradle.kts`, a dependency between modules | `docs/architecture/module-structure.md`, `docs/architecture/stack.md` |
| A `ViewModel`, `UiState`, `UiEvent`, `UiAction` | `docs/architecture/state-management.md` |
| A use case, repository, data source, DTO, mapper | `docs/architecture/use-cases.md`, `docs/architecture/data-sources.md` |
| Koin modules, `single`/`factory`/`viewModel` | `docs/architecture/dependency-injection.md` |
| A route, `NavKey`, `Navigator`, a screen entry | `docs/architecture/navigation.md` |
| `launch`, `async`, `Flow`, `try`/`catch`, `runCatching` | `docs/architecture/coroutine-error-handling.md` |
| A new screen or feature | `docs/architecture/new-feature-checklist.md` |
| A test, a fake, a `:testing` module | `docs/testing/README.md` and the guides it links |
| A user action, a `UiEvent`, `docs/analytics` | `docs/analytics/README.md` |
| `.github/`, `fastlane/`, `scripts/` | `docs/ci.md`, `docs/code-quality.md` |

Leave out anything ktlint already enforces (formatting, and the custom `bible-planner-style` rules in
`tools/ktlint-custom-rules`): the `static-analysis` workflow catches those. Your job is what a linter
can't see.

## 3. Check the product invariants

These rules hold across the whole app. A change that breaks one is a finding even when the code is
otherwise clean.

- **Pro comes from RevenueCat.** A Pro feature is unlocked only when `IsProUser` says so, from the
  RevenueCat entitlement or the Pro verification flag being off. The RevenueCat App User ID is the
  Supabase user id. Nothing grants Pro on the client by other means or adds a column to do it: free
  Pro is a promotional entitlement in RevenueCat.
- **The server decides the AI study quota.** The free limit, its weekly renewal and the rewarded
  daily limit are enforced by the backend. The client shows what the server returns and only falls
  back to Remote Config values for display when the status is unavailable. It never lets a
  generation through on its own.
- **The rewarded path mirrors the free path.** A rewarded unlock runs the same generation as a free
  one, with `isRewarded = true`, and a rewarded generation that fails gives a free retry. Ads only
  appear as that opt-in video: no banners or interstitials.
- **Remote Config switches stay safe.** Every flag or limit has a code default that keeps the
  feature off or at its current behavior (`rewarded_ads_enabled` defaults to `false`, a limit of 0
  turns the feature off), so the console can turn it off without a release.
- **Published app versions keep working.** A change to a DTO, a backend contract, a Remote Config
  key or an analytics name must not break versions already in the stores. A field the client now
  requires needs the backend deployed first. A renamed Remote Config key keeps the old one.
- **The user's data is never lost.** The app is offline first: local writes are the source of truth
  until they sync, and sync resolves conflicts by last write wins on timestamps from
  `CurrentTimestampProvider`, never the raw device clock. Sync pushes run only for an authenticated
  user.
- **The app never signs the user out on its own.** Logout happens only on an explicit user action
  or once the server confirms the session was revoked.
- **Realtime runs only in the foreground**, behind `AppForegroundStateHolder`.
- **Every platform runs.** A capability a platform lacks (ads, billing, crash reporting on desktop or
  web) gets a no-op implementation, never a crash or a missing binding.

## 4. Check the change against its intent

With the intent in hand, look for:

- Behavior the intent promises that the diff doesn't deliver, or delivers only for some paths
  (another platform, a logged-out user, a free user, offline, an empty list, a second device).
- Behavior the diff changes that the intent doesn't mention: an unrelated screen, a shared use case
  whose other callers now behave differently.
- Edge cases the intent lists that have no code or test covering them.
- A business rule that contradicts the invariants above, or another place in the code that already
  implements the same rule differently.

## 5. Report

Report only problems the change introduces, not ones that were already there. Prefer a short list of
real problems to a long list of maybes: if you are not confident, read more code until you are, or
leave it out.

For each finding, give:

- `path/to/File.kt:<line>`, the line in the new version of the file
- What is wrong, in one or two sentences
- The rule it breaks: a link to the doc section, the invariant, or the part of the intent
- What to do instead

Group the findings under **Must fix** (bugs, broken invariants, intent not met) and **Should fix**
(conventions). End with one line saying whether the change does what the intent describes. If
there is nothing to report, say "No findings" and that line.
