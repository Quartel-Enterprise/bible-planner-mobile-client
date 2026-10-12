---
name: write-tests
description: "Write the unit tests for a change from a fresh context, against what the change is meant to do rather than what the code happens to do. Use when the change needs 3 or more new unit test cases, as the \"Who writes the unit tests\" section of docs/ai_agents.md counts them; not for a bug's regression test. Writes only test code and reports suspected bugs in the production code instead of fixing them. Pass the intent, the classes to cover and the worktree path as the argument."
argument-hint: "[intent, classes to cover, worktree path]"
context: fork
agent: general-purpose
background: false
---

# Write tests

You are writing the tests for someone else's change. You don't see the conversation that produced
it, and that is the point: the author's tests tend to restate their own code, bugs included. Derive
the cases from what the change is meant to do, then check the code against them.

## The request

<request>
$ARGUMENTS
</request>

It should give you:

- **The intent**: what the change is meant to do, and the edge cases that matter (logged out, free vs
  Pro, offline, an empty list, an error from the server, another platform).
- **The scope**: the classes to cover.
- **The worktree path**, when the change lives in one. Point every file tool at it, and run Gradle
  and git with `./gradlew -p <worktree>` and `git -C <worktree>`: a `cd` doesn't persist between
  Bash calls, so a bare `./gradlew` builds the main checkout.

If the intent is missing, stop and say so: without it, the tests can only restate the code. If the
scope is missing, take the production classes the branch adds or changes
(`git diff $(git merge-base origin/main HEAD) --stat`, plus `git ls-files --others --exclude-standard`).

## 1. Read the conventions

Read [docs/testing/README.md](../../../docs/testing/README.md) and, in full,
[given-when-then.md](../../../docs/testing/given-when-then.md),
[prepare-scenario.md](../../../docs/testing/prepare-scenario.md) and
[fakes-and-coroutines.md](../../../docs/testing/fakes-and-coroutines.md). Read
[compose-ui-tests.md](../../../docs/testing/compose-ui-tests.md) only when the scope includes a
screen's content.

Then read the agent memories: `scripts/agent-memory-dir.sh` prints their folder, and `MEMORY.md`
there is the index. The `feedback_*` ones are conventions with the same weight as the docs. Open the
`project_*` ones that relate to the classes in scope.

Before writing a fake, look for one in the `:testing` modules listed in fakes-and-coroutines.md, and
read one or two existing test files of the same module to match their style.

## 2. List the cases before reading the implementation

Read the public API of each class in scope (constructor, public functions, the `UiState`,
`UiEvent` and `UiAction` of a ViewModel), not its bodies yet. From the intent alone, write down the
cases: each behavior it promises, and each edge case it names or clearly implies.

Then read the implementation and add a case for each branch the list doesn't reach yet: an error
path, an early return, a `when` arm. A branch the intent doesn't explain is worth a line in the
report (step 5), since it may be behavior nobody asked for.

## 3. Write the tests

- Write only test code: files under a `src/*Test/` source set, and new shared fakes in a `:testing`
  module when more than one module needs them. Never edit production code, not even to make it
  testable. Report what blocks a test instead.
- One test per case, named and structured as the docs say.
- Assert what the intent promises. Never weaken an assertion until it matches what the code does.

## 4. Run them

```bash
<worktree>/scripts/ktlint.sh --format
./gradlew -p <worktree> :<module>:jvmTest --continue
```

Run `jvmTest` for each module you wrote tests in. If ktlint reports something it can't fix, fix it
by hand and run it again.

When a test fails, decide which side is wrong:

- **The test** (a wrong fake, a missing `advanceUntilIdle`, a misread API): fix the test.
- **The code** (it contradicts the intent): keep the test as it is, failing, and report it as a
  suspected bug. Don't `@Ignore` it and don't change the code: the author decides.

Leave out the coverage tasks (`koverVerifyCi`, `verifyNewFilesCoverage`): `create-pr` runs them for
the whole branch.

## 5. Report

- **Tests written**: each file, with one line per case it covers.
- **Suspected bugs**: for each, `path/to/File.kt:<line>`, what the code does, what the intent says
  instead, and the failing test that shows it.
- **Behavior the intent doesn't mention**: branches you tested as the code has them, but that may be
  unintended.
- **Left out**: cases you couldn't test and why (a platform boundary such as an `expect class`, a
  dependency without an interface to fake).
- The last `jvmTest` result for each module: passed, or which tests fail.
