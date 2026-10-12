# Bible Planner — Architecture Guide for AI Assistants

This document describes the architecture conventions of this project. Follow these patterns exactly when creating new features or modifying existing ones.

- [@docs/architecture/stack.md](architecture/stack.md)
- [@docs/architecture/module-structure.md](architecture/module-structure.md)
- [@docs/architecture/state-management.md](architecture/state-management.md)
- [@docs/architecture/use-cases.md](architecture/use-cases.md)
- [@docs/architecture/dependency-injection.md](architecture/dependency-injection.md)
- [@docs/architecture/navigation.md](architecture/navigation.md)
- [@docs/architecture/data-sources.md](architecture/data-sources.md)
- [@docs/architecture/new-feature-checklist.md](architecture/new-feature-checklist.md)
- [@docs/architecture/pr-guidelines.md](architecture/pr-guidelines.md)
- [@docs/architecture/code-style.md](architecture/code-style.md)
- [@docs/architecture/dry.md](architecture/dry.md)
- [@docs/architecture/coroutine-error-handling.md](architecture/coroutine-error-handling.md)
- [@docs/analytics/README.md](analytics/README.md)
- [@docs/testing/README.md](testing/README.md)
- [@docs/performance.md](performance.md)
- [@docs/store-listing-screenshots.md](store-listing-screenshots.md)
- [@docs/store-listing-metadata.md](store-listing-metadata.md)
- [@docs/getting-started.md](getting-started.md)
- [@docs/agent-cli.md](agent-cli.md)
- [@docs/code-quality.md](code-quality.md)
- [@docs/ci.md](ci.md)

To check a ViewModel, use case or repository change in the running app without a simulator, drive it
headlessly with `scripts/agent-cli.sh` (see [docs/agent-cli.md](agent-cli.md)).

Write code, comments, docs, commit messages and pull requests in English, even when the conversation is in
another language. User-facing strings are the exception: they are translated in each `strings.xml`.

## Who writes the unit tests

Once the production code compiles, and before `create-pr`, decide who writes the unit tests. The
`write-tests` skill writes them in a subagent that doesn't see this conversation: it lists the cases
from the intent you hand it, not from the code you just wrote, so it catches what your own tests
would restate. Reading the testing docs, the fakes and the module's style costs it a few minutes,
which only pays off when there are enough cases.

Look only at the production code the change adds or changes that Kover measures (see
[What is measured](code-quality.md#what-is-measured)): not composables, `di` packages, `*Root` and
`*Navigation` entries, interfaces, models without logic, or `androidMain`/`iosMain`/`mobileMain`.
List the cases that code needs and no existing test covers yet: a new class with logic (use case,
repository, mapper, data source, coordinator, ViewModel), a new public function, a newly handled
`UiEvent`, a new `if`/`when`/`catch`/early-return branch that changes the result.

| The change | Who writes the tests |
| --- | --- |
| 3 or more new cases | `write-tests` |
| 1 or 2 new cases | you |
| A bug fix | you write the regression test, first, failing before the fix: you reproduced the bug. New behavior the fix adds beyond that follows the rows above |
| A behavior change that existing tests now contradict | you update those assertions, since you know what changed on purpose. New cases follow the rows above |
| No new case: a refactor whose existing tests pass unchanged, or only code Kover leaves out | nobody: the existing tests are the spec, and screens get UI or screenshot tests, not unit tests |

Calling `write-tests`:

- One call per module, so each run reads only that module's fakes and style. Run them one after the
  other: they build the same checkout.
- Pass the intent as a reviewer would need it: what the change is meant to do, the edge cases that
  matter (logged out, free vs Pro, offline, empty, a server error, another platform), and any
  decision taken in this conversation. Not how the code does it: that is what the tests check.
- Pass the classes to cover and the worktree path, if any.
- For each suspected bug it reports, fix the code. If the intent you passed was wrong instead, fix
  the test and tell the user.
