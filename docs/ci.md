# CI

## Gradle cache

[`.github/actions/gradle`](../.github/actions/gradle/action.yml) sets up JDK 21 and
`gradle/actions/setup-gradle`. Every Gradle job goes through it, and it decides who may write to the
repository's 10 GB cache budget:

- **`main`** always writes. Every pull request restores from those entries.
- **`GRADLE_CACHE_WRITE_ON_PULL_REQUESTS=true`** lets a job write on a pull request too, scoped to
  that pull request's ref, so the next push compiles only what changed. Only `build`, the longest
  job, sets it. `unit-tests` stays read-only and restores `main`'s entry.
- **`GRADLE_CACHE_READ_ONLY=true`** never writes, even on `main`. It is for jobs that barely use
  Gradle (`ktlint`, `module-graph`): their small entries would take up the budget, and since
  setup-gradle falls back to the most recent entry of any job, another job could restore one of
  them instead of a useful one.

On a pull request, every job except the read-only ones restores only its own entry. Without that,
the fallback picks the newest entry of any job: `unit-tests` on #464 restored the one `ktlint` had
saved on `main`, which holds little more than the ruleset jar, and ran all 1227 of its tasks with
no cache hit. On `main` the fallback stays on, so a new job's first run starts from another job's
entry instead of from nothing.

Pull request entries are never cleaned up while the pull request is open, because each push would
save a new entry of hundreds of MB. When it closes, `cleanup-pr-caches` cancels its runs that are
still going and waits for them, since a cancelled job still saves its cache in the post step, and
then deletes the entries of every closed pull request.

The `GRADLE_ENCRYPTION_KEY` secret encrypts the configuration cache. Without it the configuration
cache is dropped between runs and every job configures the build from scratch.

R8 takes about two thirds of the `build` job and reruns whenever its inputs change. Only the
release workflow's build, the one signed with the release keystore, embeds the commit and a
Crashlytics mapping file id and uploads its mapping file. Every other release build leaves both out:
the id is random per build and the commit changes on every run, so either would keep R8 from ever
coming from the cache, even for a pull request that doesn't touch the app's code.

The ktlint action caches its own things: the CLI, keyed by version, and the custom ruleset jar,
keyed by its sources. It sets up Gradle only when the jar has to be rebuilt.

## Merge when green

Adding the `merge-when-green` label to a pull request makes the `merge-when-green` workflow squash
merge it once every check on its head commit has passed, then remove the label. GitHub's own
auto-merge is not enough here: it waits only for the required checks, and `ui-tests` is not one of
them.

The workflow runs when the label is added and each time a pull request workflow completes, so the
last check to finish is the one that triggers the merge. A push to the pull request moves its head
commit and the new checks have to pass again. A failed check leaves the pull request open with the
label on; re-running the check and getting it green merges it.

The merge uses the `MERGE_WHEN_GREEN_TOKEN` secret, not `GITHUB_TOKEN`: a merge made with
`GITHUB_TOKEN` triggers no other workflow, so the push to `main` would run no checks and
`cleanup-pr-caches` would not delete the pull request's caches. The secret is a fine-grained personal
access token restricted to this repository, with Contents and Pull requests set to Read and write.
The merge shows up as made by the token's owner. When the token expires, the workflow fails with an
error that names the secret.

When a workflow is added to or renamed in the pull request checks, update the `workflow_run` list in
`merge-when-green.yml` too. A workflow missing from it still blocks the merge while running, but its
completion does not trigger one.

## AI review

Claude reviews pull requests at three points:

- **Before it opens**, the `create-pr` skill runs `/code-review high` for correctness bugs and the
  project's `review-conventions` skill, which checks the change against the docs under `docs/`, the
  product invariants listed in the skill, and the pull request description written before the
  review. Findings stop the skill until the author fixes them or chooses to go on. It is skipped
  when the author asks, and when the branch only changes docs, release notes, the version, the
  version catalog or store metadata. This runs on the author's Claude subscription.
- **On GitHub, when it opens**, `claude-review` posts the findings of the official `code-review`
  plugin as inline comments, as a second opinion. A draft is reviewed when it is marked ready for
  review instead. The review skips a pull request it has already commented on, so pushes after the
  pull request opens, and reopening it, don't review it again.
- **On GitHub, on request**, adding the `ai-review` label runs `claude-review-on-request`, which
  reviews the pull request again with the same plugin, then removes the label. The plugin can't
  read inline comments, so the workflow hands it the ones Claude left before, for it not to raise
  the same issues again. Use it after new pushes. When another review of the pull request is still
  running, it starts nothing and only removes the label.

Both only comment: they fail only when the run itself does (an expired token, the 30-minute
timeout), never because of what they found. They are skipped on pull requests from forks, which get
no secrets, and the `ai-review` label then has to be removed by hand. The action also refuses to run
when the pull request changes the workflow file that started it: the file has to match the one on
`main`, so a change to either workflow can only be tried out after it merges. The run then ends
green with a "workflow validation" warning and posts nothing. The review step they share, in
`.github/actions/claude-review`, is not checked: a change to it runs on the pull request that makes
it, with the token. Only branches of this repository get there, since forks are skipped.

Treat them as advisory, not as a gate: their findings never fail a check. Since `claude-review` runs
on the head commit the pull request opened with, `merge-when-green` waits for it to finish there,
and a run that failed (an expired token) holds the merge until it is rerun. A later push brings a
head commit with no review check, so the merge no longer waits for it.

The action hides Claude's output, since the logs of this repository are public. A tool the review
was denied still shows as a warning on the run, with only its name, and for a Bash command its
program and subcommand: allow it in `.github/actions/claude-review` when the review needs it.

The review on request is a workflow of its own because every label added to a pull request starts
it, and the job of a label other than `ai-review` is skipped. Its job is named `review-on-request`,
so those skipped checks never hide the `review` check of the opening review. They do hide a review
on request still running: `merge-when-green` reads only the latest check of each name, so adding a
label after `ai-review`, `merge-when-green` included, lets the pull request merge without waiting
for that review.

It authenticates with the `CLAUDE_CODE_OAUTH_TOKEN` secret, created with `claude setup-token` on the
maintainer's account, so the reviews count against that Claude subscription instead of being billed
per token. When the token expires or is revoked, the run fails on authentication: create a new one
and update the secret. The action posts as the Claude GitHub App, which has to be installed on the
repository.

Anthropic's managed Code Review is not used: it is only available on Team and Enterprise plans, and
enabling it next to these workflows would review every pull request twice.

## UI tests

The `ui-tests` workflow runs the [Compose UI tests](testing/compose-ui-tests.md) of every module
that has a `src/androidDeviceTest` directory. `scripts/ui_test_shard.sh` finds those modules, so a
new one joins without editing the workflow. `unit-tests` in `build-and-test` passes
`-PuiTests=exclude` and leaves them to this workflow, so each job reports one kind of test.

- **`desktop`** runs them on the JVM with `-PuiTests=only`, which leaves the unit tests of the same
  modules to `unit-tests`. Only the modules with UI tests and what they depend on compile here.

- **`android`** has two shards, and each boots its own API 35 `x86_64` emulator with KVM. The
  script deals the modules to the shards round-robin, so keep `SHARD_COUNT` equal to the length of
  `matrix.shard`. A shard assembles its test APKs before the emulator boots: that gives Gradle all
  four cores, and a compile error fails the job before the SDK download and the boot. The emulator
  and its system image are installed in their own step, with three attempts, because the emulator
  runner fails the job on the first corrupt download. The tests run with `--max-workers=1`, so one
  module at a time drives the emulator and the log stays readable. The step has a 25-minute
  timeout, under the job's 45: a hung run then still uploads its reports.
- **`ios`** runs the same modules on the iOS simulator of a `macos-latest` runner, which costs
  nothing on a public repository. Besides setup-gradle it caches `~/.konan`, which setup-gradle
  leaves out: the Kotlin/Native toolchain and, more expensive, the compiled cache of every library
  the test binaries link. Building that cache is about 15 minutes of a cold run, on its first link.
  The key follows the Kotlin version and the version catalog and falls back to the last entry for
  the same Kotlin version, so a library bump rebuilds only what changed. It is the slowest job of a
  pull request, so it also writes its Gradle cache there.

The device jobs run the rest of `commonTest` too, since `androidDeviceTest` and `iosTest` depend on
all of it. Every job uploads its test reports when it fails.

`:shared` joins the `desktop` and `android` jobs with the [end-to-end flows](testing/end-to-end-tests.md),
and the script leaves it out of `ios`: the flows switch tabs, which on iOS are a native `UITabBar`.

## iOS release link

Most of the release workflow's `ios-build` job is one Gradle task,
`:shared:linkReleaseFrameworkIosArm64`. Compiling the 79 modules it depends on takes under five
minutes from a cold cache; the link takes the rest. A release framework is built with link-time
optimization, every module compiled and optimized together, and for this app that needs about
13 GB in the Gradle daemon and another 3 to 6 GB in the linker it starts. No standard runner has
that much, so how long the link takes follows from the machine, not from the build's settings:

| Machine | Link | Swap in use |
|---|---|---|
| `macos-latest`: M1, 3 cores, 7 GB | 26 min | 4.4 GB |
| `macos-latest`: M2 Pro, 5 cores, 14 GB | 10 min | none |
| `macos-15-intel`: 4 cores, 14 GB | 22 min | 4.2 GB |
| A developer's M4 Max, 14 cores, 36 GB | 5 to 6 min | |

`macos-latest` hands out either of the first two. Of 21 jobs measured in one evening, on
2026-09-28, 7 were given the M2 Pro; the pinned labels (`macos-26`, `macos-15`, `macos-14`) gave
the M1 in all 25. So `ios-build` stays on `macos-latest`, and its first step says on the run's page
which machine it got.

What was measured and ruled out, each of them once, with the heap of 8 GB `gradle.properties`
asks for as the baseline:

| Change | On the M1 runner | On the Intel runner | On the M4 Max |
|---|---|---|---|
| Heap of 6 GB | 16% slower | 43% slower | 8% slower |
| Heap of 5 GB | | | runs out of heap |
| Heap of 12 GB | | | no change |
| Parallel collector, heap of 6 GB | not done after an hour | | not done after 18 minutes |
| G1 returning unused heap (`G1PeriodicGCInterval`, `MaxHeapFreeRatio`) | no change | 21% slower | no change |

The last one does lower the daemon's peak from 13 GB to 8.7 GB on the M4 Max. With 7 GB the link
swaps either way. The Intel runner is no way out either: its link is four minutes shorter, and
everything else on it is slower.

What would shorten it, each at a price that is not the build's to pay on its own:

- **A larger runner.** `macos-latest-xlarge` is the M2 Pro above, every time. It is billed per
  minute on a public repository too, and needs a plan that offers larger runners.
- **The release binary cache** (`kotlin.native.binary.enableReleaseBinaryCache`). Each module is
  compiled into a cache of its own instead of all together: 10 minutes on the M1 instead of 26,
  7 on the M2 Pro instead of 10, 37 seconds locally once the caches exist. It gives up link-time
  optimization across modules, the static framework grows from 287 MB to 444 MB, and Kotlin's
  documentation still calls its runtime performance a work in progress. That changes what ships.

Xcode buffers the output of the build phase that runs Gradle, so the release's log cannot tell
compiling from linking. To measure the link again, run the task on its own with `--profile`.

## Adding a workflow

- Scope it with `paths` or `paths-ignore`, so a docs-only change doesn't run a build — unless it is
  a required check, which GitHub never reports for a workflow skipped by path filtering.
- Use per-commit `concurrency` groups on `main` if it writes a Gradle cache, per-ref otherwise: a
  cancelled run saves no cache.
- Set up Gradle through `./.github/actions/gradle`, pass `secrets.GRADLE_ENCRYPTION_KEY`, and pick
  one of the two cache switches above if the job is not a default reader.
