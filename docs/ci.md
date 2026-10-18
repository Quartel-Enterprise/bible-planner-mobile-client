# CI

## Gradle cache

[`.github/actions/gradle`](../.github/actions/gradle/action.yml) sets up JDK 21 and
`gradle/actions/setup-gradle`. Every Gradle job goes through it, and it decides who may write to the
repository's 10 GB cache budget:

- **`main`** always writes. Every pull request restores from those entries.
- **`GRADLE_CACHE_WRITE_ON_PULL_REQUESTS=true`** lets a job write on a pull request too, scoped to
  that pull request's ref, so the next push compiles only what changed. The jobs on the critical
  path set it: `build`, the `android` shards and `ios` of `ui-tests`. `unit-tests` stays
  read-only and restores `main`'s entry.
- **`GRADLE_CACHE_READ_ONLY=true`** never writes, even on `main`. It is for jobs that barely use
  Gradle (`module-graph`): their small entries would take up the budget, and since
  setup-gradle falls back to the most recent entry of any job, another job could restore one of
  them instead of a useful one.

On a pull request, every job except the read-only ones restores only its own entry. Without that,
the fallback picks the newest entry of any job: `unit-tests` on #464 restored the one `ktlint` had
saved on `main`, which holds little more than the ruleset jar, and ran all 1227 of its tasks with
no cache hit. On `main` the fallback stays on, so a new job's first run starts from another job's
entry instead of from nothing.

`cleanup-pr-caches` keeps a pull request's entries within what its latest push uses, and deletes
them all when it closes. Every push saves a new transforms and build cache bundle per writer job,
hundreds of MB each, that the next push no longer needs, since it restores the newest home entry of
its job and that one names the newest bundles; before this, three pushes of one pull request held
12.5 GB of the budget and had pushed out `main`'s entries for `build`, the `android` shards and
`ios`, so every new pull request started cold. Once both writer workflows of a push have succeeded,
the `prune` job runs [`scripts/prune_pr_caches.sh`](../scripts/prune_pr_caches.sh) with the time
that push's runs started; the rule, what stays and what goes and the cases it cannot tell apart, is
in the script's header. It can be tried by hand without deleting anything:

```bash
./scripts/prune_pr_caches.sh --dry-run refs/pull/585/merge 2026-10-11T12:57:50Z
```

When the pull request closes, the `cleanup` job cancels its runs that are still going and waits
for them, since a cancelled job still saves its cache in the post step, and then runs the script
with `--closed`, which deletes the entries of every closed pull request.

The `GRADLE_ENCRYPTION_KEY` secret encrypts the configuration cache. Without it the configuration
cache is dropped between runs and every job configures the build from scratch.

R8 takes about two thirds of the `build` job and reruns whenever its inputs change. Only the
release workflow's build, the one signed with the release keystore, embeds the commit and a
Crashlytics mapping file id and uploads its mapping file. Every other release build leaves both out:
the id is random per build and the commit changes on every run, so either would keep R8 from ever
coming from the cache, even for a pull request that doesn't touch the app's code.

The ktlint job uses the action from ktlint-rules and needs no Gradle: it caches the ktlint CLI and the
ruleset jar, each keyed by its own version.

## Merge when green

Adding the `merge-when-green` label to a pull request makes the `merge-when-green` workflow squash
merge it once every check on its head commit has passed, then remove the label. GitHub's own
auto-merge is not enough here: it waits only for the required checks, and `ui-tests` is not one of
them.

The workflow runs when the label is added and each time a pull request workflow completes, so the
last check to finish is the one that triggers the merge. A push to the pull request moves its head
commit and the new checks have to pass again. A failed check leaves the pull request open with the
label on; re-running the check and getting it green merges it.

A job waiting on another has no check until it starts, so right after `review-gate` passes every
check of a pull request can be green while its workflows still have jobs to start. A run of a
workflow that calls `review-gate` therefore counts as unfinished until it completes, and its
completion starts `merge-when-green` again; the list of those workflows is in the merge step.

A green pull request with an unresolved review conversation is not merged: the workflow removes the
label and comments on the pull request instead. See [Unresolved conversations](#unresolved-conversations).

The merge uses the `MERGE_WHEN_GREEN_TOKEN` secret, not `GITHUB_TOKEN`: a merge made with
`GITHUB_TOKEN` triggers no other workflow, so the push to `main` would run no checks and
`cleanup-pr-caches` would not delete the pull request's caches. The secret is a fine-grained personal
access token restricted to this repository, with Contents and Pull requests set to Read and write.
The merge shows up as made by the token's owner. The `record-screenshots`
workflow (see [Screenshot tests](#screenshot-tests)) pushes with the same token. When the token expires, the workflow fails with an
error that names the secret.

When a workflow is added to or renamed in the pull request checks, update the `workflow_run` list in
`merge-when-green.yml` too, and, when it calls `review-gate`, the list of those workflows in its
merge step. A workflow missing from it still blocks the merge while running, but its
completion does not trigger one. When it is one of the workflows that write the Gradle cache on pull
requests, update the `workflow_run` list and the `WRITERS` variable of `cleanup-pr-caches.yml` as
well, or the pruning stops without an error.

## AI review

Claude reviews pull requests at three points:

- **Before it opens**, the `create-pr` skill runs `/code-review high`, which looks for correctness
  bugs and for cleanups of the changed code (reuse, simplification, efficiency), and the project's
  `review-conventions` skill, which checks the change against the docs under `docs/`, the
  product invariants listed in the skill, and the pull request description written before the
  review. Findings stop the skill until the author fixes them or chooses to go on. It is skipped
  when the author asks, and when the branch only changes docs, release notes, the version, the
  version catalog or store metadata. This runs on the author's Claude subscription.
- **On GitHub, when it opens**, `claude-review` runs the same `/code-review high`, on Opus, with
  `--fix`: a second opinion on the code the author pushed, which applies its findings and pushes
  them to the branch as a commit of its own. It then posts the findings as one review: the fixed
  ones in its body, the ones it left for the author as an inline comment each, labelled with its
  severity and, when the fix fits the commented lines, a suggestion to commit. The workflows that
  build and test the app wait for it (see [Fixes and the review gate](#fixes-and-the-review-gate)).
  A draft is reviewed when it is marked ready for review instead. Pushes after the pull request
  opens, its own fix commit included, and reopening it, don't review it again.
- **On GitHub, on request**, adding the `ai-review` label runs `claude-review-on-request`, which
  reviews the pull request again the same way, only commenting, then removes the label. Use it
  after new pushes.
  When another review of the pull request is still running, it starts nothing and only removes the
  label.

The review can't read the earlier reviews on the pull request, so the action hands it the findings
they left, inline comments and review bodies alike (a draft marked ready a second time, a review on
request), for it not to raise the same issues again.

Both fail only when the run itself does (an expired token, the 30-minute timeout), never because of
what they found. They are skipped on pull requests from forks, which get
no secrets, and the `ai-review` label then has to be removed by hand. The action also refuses to run
when the pull request changes the workflow file that started it: the file has to match the one on
`main`, so a change to either workflow can only be tried out after it merges. The run then ends
green with a "workflow validation" warning and posts nothing. The review step they share, in
`.github/actions/claude-review`, is not checked: a change to it runs on the pull request that makes
it, with the token. Only branches of this repository get there, since forks are skipped.

Their findings never fail a check, but each one left for the author is an inline comment, and an
unresolved comment blocks the merge (see [Unresolved conversations](#unresolved-conversations)). Since `claude-review`
runs on the head commit the pull request opened with, `merge-when-green` waits for it to finish
there, and a run that failed (an expired token) holds the merge until it is rerun. A later push
brings a head commit with no review check, so the merge no longer waits for it.

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

Both reviews also read the maintainer's agent memories. This repository is public, so they live in
the private repository `PierreVieira/bible-planner-agent-memory`. The review step checks out only its
`feedback_*` files, the conventions asked for in past sessions, into `.agent-memory` with the
`AGENT_MEMORY_DEPLOY_KEY` secret, the private half of a read-only deploy key of that repository. The
`project_*` memories and the index hold figures and ids, so they never reach the runner, and the
review is told never to quote a convention in a comment. Without the secret, or when the checkout
fails, the review runs without them and the run shows a warning. The skill's own agents judge
findings against `CLAUDE.md`, so how much weight the memories get on GitHub is up to its
orchestrator.

Locally, the `review-conventions` skill reads every memory from Claude Code's memory folder
(`scripts/agent-memory-dir.sh`), and `create-pr` and `finish-task` push them to the private repository
with `scripts/sync-agent-memory.sh`.

Both GitHub reviews run the `/code-review` skill bundled with Claude Code, the one `create-pr` runs,
so the three reviews share one recipe. All three pass `--max-findings all`: by default the skill keeps
only the ten most severe findings, so every round of fixes on a large pull request surfaced the next
ten and needed another review on request (#585 took 27 rounds). How many `all` reports is up to
the recipe: locally, where it reports through a tool, high effort still stops at 32; on GitHub,
where it ends with JSON, the recipe sets no maximum. The skill remembers the choice for later runs
on the same machine, so after `create-pr` a `/code-review` typed by hand reports every finding too,
until it is given `--max-findings default`. At high effort it looks for correctness bugs, `CLAUDE.md`
violations and cleanups of the changed code (reuse, simplification, efficiency, a fix made at the
wrong depth), as far as the variant of the recipe for the model covers them. The model is what
differs: Opus on GitHub (`--model` in `.github/actions/claude-review`), the author's session model
locally. On GitHub the model invokes the skill itself with the level, the pull request number and
`--max-findings all` (plus `--fix` for the opening review), and ends its reply with the findings as
JSON; the model posts nothing. The
skill's findings carry no severity and no fix, so the model adds three fields to each one: a
`severity`, a `fix` and, only when replacing the commented lines fully fixes it, a `suggestion`
with their new text. Each comment opens with the severity: 🔴 **Must fix** (a bug users would hit,
a security hole, a race that corrupts state), 🟡 **Should fix** (a real problem with a limited
impact, or a convention the change breaks) or ⚪ **Nitpick** (optional), and the review's summary
counts them; the findings are posted most severe first. Nitpicks are inline comments too, so they
hold the merge until resolved: one in the review body would lose its suggestion's commit button.
A suggestion becomes a GitHub suggestion block, which the author can commit from the pull request,
only on a comment anchored to the exact lines the finding names. Anchored anywhere else, GitHub
would apply it to the wrong lines, so it is shown as a plain code block naming the lines it was
written for. The model reads those lines from the reviewed commit, since with `--fix` the working
tree holds its own changes, and a finding it fixed gets no suggestion. A review body is kept under
the 65,536 characters GitHub allows: past that, the findings it lists lose their suggestions, then
the last ones are left out, with a link to the run. A Stop hook of the session
(`.github/actions/claude-review/stop-hook.sh`) then hands that JSON to `post-review.sh`, which posts
it as one review with an inline comment per finding, one notification, rather than through the
skill's `--comment` flag, which posts one review per finding. The hook runs inside the session, so
the review comes from `claude[bot]`, with the Claude GitHub App's token the action gives the session
and revokes when it ends. When the hook didn't post, a step of the action posts the same findings with
the workflow's token, as `github-actions`, and the run shows a warning.
The review is anchored to the head commit the workflow checked out, so a push during the review
doesn't move the comments. GitHub only anchors a comment to a line inside the diff, so a finding on
another line of a changed file (the recipe reports bugs on unchanged lines of a touched function on
purpose) is anchored to the nearest changed line of that file, naming its own line, and still opens a
thread; a finding in a file the diff doesn't touch goes in the review's body, which opens no thread
and so doesn't hold the merge. With no findings the review says so, and a reply with no findings list
(a skill that stopped early, a session that never ran) gets a review pointing at the run, so the pull
request shows the review ran. The exception is the job's 30-minute timeout: GitHub cancels the job
and nothing is posted, so the failed check is the only signal. The scripts the hook runs are copied out of the checkout before the review,
and the model's file tools are blocked (with `fix`, editing is allowed only inside the checkout,
never under `.git`, `.github`, `.claude` or `.agent-memory`, nor on those copies), so nothing the
review does to the working tree changes what runs with the App's token. At high effort the skill runs its angles one after another in a single context, so a
large pull request may take long: the workflows give it 30 minutes.

Until October 2026 the two reviews ran the `code-review` plugin of the official marketplace instead,
a fixed recipe: five Sonnet agents, a shallow scan for bugs plus a `CLAUDE.md` check, nothing under
80% confidence, no effort level. It answered "No issues found" to most pull requests the local review
had findings on. Two things it did are gone with it: it skipped automated or trivial pull requests,
and it skipped a pull request it had already commented on (the earlier comments handed to the review
now cover that case).

### Fixes and the review gate

Until October 2026 the opening review only commented, while every other workflow ran on the same
commit. It almost always found something, so the author pushed a fix and every check ran a second
time. Now the review fixes what it found, and the checks wait for it:

- **The review fixes.** `claude-review` passes `fix: 'true'` to the action, which invokes the skill
  with `--fix` and lets the model edit the files of the checkout, never under `.git`, `.github`,
  `.claude` (settings and hooks a session would pick up) or `.agent-memory`. It can't create a file.
  It is told to fix only what is clear and contained, and to leave what needs the author's call (a
  design choice, a product behaviour, a fix that needs a new file) as it is. Each
  finding comes back with an outcome: `fixed`, `skipped` or `no_change_needed`.
- **The hook commits.** Before posting, the Stop hook runs `commit-fixes.sh`: it commits the tracked
  files the review changed, as `claude[bot]`, and pushes the commit to the branch with the Claude
  GitHub App's token. A push with that token starts the pull request's workflows, which one with
  `GITHUB_TOKEN` wouldn't. Nothing is pushed when no finding is marked fixed, when a fix touches
  `.github` (the App has no workflows permission anyway) or `.claude`, when the working tree has an
  untracked file (a fix that would be pushed without it), or when the branch moved during the
  review: the push is never forced. The review then lists the fixed findings at the end of its body, under
  the fix commit, so they open no thread, and only the others as inline comments. Without a push,
  every finding is an inline comment, as before.
- **The checks wait.** `build-and-test`, `ui-tests`, `screenshot-tests`, `static-analysis` and
  `module-graph` start with a `review-gate` job, the reusable `review-gate.yml`. On the commit a
  pull request opened with, and on the review's fix commit (a push by `claude[bot]`), it waits for
  the `claude-review` run of the reviewed commit to end, checking once a minute: five workflows wait
  on the same review with `GITHUB_TOKEN`'s 1,000 requests an hour. When the pull request's head moved
  meanwhile (the fix commit), it skips the other jobs: that push started them again on the new
  commit, and cancels this run anyway. Every other event (an author's push, a reopened pull request,
  `main`) skips the gate job itself, so it starts no runner, and the other jobs run on a skipped
  gate. A draft or a fork passes it right away, and so does a failed or cancelled review, GitHub's
  API failing three times in a row, or a review still running after 40 minutes. `translations`
  doesn't wait: `check-translations` is required and takes seconds.
- **The checks are held for open threads.** Once the review ended and the head didn't move, the
  gate counts the review's unresolved threads (`claude[bot]`'s, or `github-actions[bot]`'s when the
  fallback step posted). With one open, the other jobs are skipped and the gate fails, in its `Hold
  for the open threads` step, so the pull request shows why nothing ran and `merge-when-green`
  doesn't take the skipped checks for green. The author is about to push the fixes, and that push
  runs the checks. Resolving the threads without a push (a finding answered instead of fixed)
  starts no workflow: add the `run-checks` label, and `run-checks.yml` re-runs every held run of the
  head commit, whose gate then lets the checks through. With a thread still open it re-runs nothing
  and comments instead. Either way it removes the label. `merge-when-green` re-runs them too, see
  [Unresolved conversations](#unresolved-conversations). On the fix commit the review is seconds
  from ending, so the gate checks on it every 10 seconds instead of every minute.

`scripts/review_gate.sh` holds what these workflows have to agree on: which threads count as the
review's and which runs were held (a failed `Hold for the open threads` step).

So a pull request whose review found nothing runs its checks once, after the review; one whose
review fixed everything it found runs them once, on the fix commit; one whose review left threads
runs them once, on the author's next push or on `run-checks`. Either way the checks finish later
than when they ran next to the review, but they never run twice.

The review's checkout doesn't persist `GITHUB_TOKEN` (`persist-credentials: false`): checkout keeps
its header in a file of its own, which `claude-code-action` doesn't find when it swaps in the App's
token, so git kept sending the read-only token and the fix push was refused (#600, the first pull
request after the fixes shipped). A refused push now logs git's error lines, with any credentials
in a url masked.

The fix commit isn't reviewed again; the `ai-review` label reviews it, only commenting. It isn't
built before the push either: the review has no Gradle, so a fix that breaks the build or ktlint
shows up in the checks of the fix commit. And the local branch is one commit behind afterwards:
pull before pushing to it again.

Anthropic's managed Code Review is not used: it is only available on Team and Enterprise plans, and
enabling it next to these workflows would review every pull request twice.

### Unresolved conversations

The `Main rules` ruleset requires every review conversation to be resolved before a pull request
merges into `main` ("Require conversation resolution before merging", with no approvals required).
GitHub greys out the merge button while one is open, and `merge-when-green` checks first: when
everything else is green, it removes its label and comments on the pull request instead of failing
on the merge.

For each Claude finding, either fix it, or reply with why the code stays as it is, then resolve the
conversation. Resolving one starts no workflow, so add the `merge-when-green` label again afterwards.
When the opening review's threads held the checks (see
[Fixes and the review gate](#fixes-and-the-review-gate)), `merge-when-green` doesn't count the failed
gates as failed checks: with a conversation still open it removes its label and comments, as above,
and once every conversation is resolved it re-runs the held runs, keeps its label and merges when
they pass. To run the held checks without merging, add `run-checks` instead.
The ruleset has no bypass, so this applies to admins too.

## UI tests

The `ui-tests` workflow runs the [Compose UI tests](testing/compose-ui-tests.md) of every module
that has a `src/androidDeviceTest` directory. `scripts/ui_test_shard.sh` finds those modules, so a
new one joins without editing the workflow. `unit-tests` in `build-and-test` passes
`-PuiTests=exclude` and leaves them to this workflow, so each job reports one kind of test.

- **`desktop`** runs them on the JVM with `-PuiTests=only`, which leaves the unit tests of the same
  modules to `unit-tests`. Only the modules with UI tests and what they depend on compile here.

- **`android`** runs in shards, three today, and each boots its own API 35 `x86_64` emulator with
  KVM. Shard 0 runs `:shared` alone, and the script deals the screen modules round-robin to the
  other shards; to add one, add the next index to `matrix.shard`, since the workflow passes the
  size of the matrix (`strategy.job-total`) to the script as the count. `:shared` is on its own
  because its [end-to-end flows](testing/end-to-end-tests.md) drive the whole app from launch and
  are by far the longest module on the device, several times any screen module, so no layout can
  make the job finish sooner than `:shared` alone takes. The screen modules together take longer
  than `:shared`, so the layout needs at least three shards to pay: measured in October 2026,
  `:shared` took 3 to 4 minutes, the nine screen modules about 6 together, and the shard that held
  `:shared` with four of them ran 2 to 3 minutes longer than the other one on every run. The
  script runs Gradle itself and fails on a shard with no module: `./gradlew` with no task would
  run `help` and pass without a test. A shard assembles its test APKs before the emulator boots:
  that gives Gradle all four cores, and a compile error fails the job before the SDK download and
  the boot. The emulator and its system image are installed in their own step, with three
  attempts, because the emulator runner fails the job on the first corrupt download. The tests run
  with `--max-workers=1`, so one module at a time drives the emulator and the log stays readable.
  The step has a 25-minute timeout, under the job's 45: a hung run then still uploads its reports.
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

`:tools:baseline-profile` is in no workflow. Its tests sit in `src/main`, as a `com.android.test`
module's do, so the script never finds it, and they are the macrobenchmarks: they run by hand on a
physical device nothing else is using. See [Performance](performance.md).

## Screenshot tests

The `screenshot-tests` workflow runs the [screenshot tests](testing/screenshot-tests.md) of every
module that calls `configureScreenshotTests()`, through their `screenshotTests` task. Its `verify`
job compares each render with its reference on every push, and uploads the differences as
`screenshot-differences` when one fails. It never skips a push: a skipped check reads as passed to
`merge-when-green`, which would let a pull request through with nothing verified.

The `record-screenshots` workflow records the references when a pull request gets the
`record-screenshots` label. It is a workflow of its own, like `claude-review-on-request`, because
every label starts it: the skipped `record` check other labels leave never stands in for `verify`.
It renders the references on the pull request's branch, commits them and only then drops the label,
so a failed run or a rejected push keeps it on for a retry. The push uses `MERGE_WHEN_GREEN_TOKEN`
(see [Merge when green](#merge-when-green)), handed only to that step: a push made with
`GITHUB_TOKEN` starts no workflow, and `main`'s ruleset waits for `check-translations` on the head
commit. Pull requests from forks get no secrets, so they can't record.

`screenshot-tests` is not a required check; `merge-when-green` still waits for it.

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
- If it builds or tests the app on pull requests, start it with the `review-gate` job, with the
  same `if` as the other callers, and make its other jobs need it (see
  [Fixes and the review gate](#fixes-and-the-review-gate)). Add it to the `GATED` list of
  `scripts/review_gate.sh` too, or neither `run-checks` nor `merge-when-green` re-runs it when the
  gate held it.
- Set up Gradle through `./.github/actions/gradle`, pass `secrets.GRADLE_ENCRYPTION_KEY`, and pick
  one of the two cache switches above if the job is not a default reader.
