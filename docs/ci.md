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

## Instrumented tests

The `instrumented-tests` workflow runs the [Compose UI tests](testing/compose-ui-tests.md), and the
rest of `commonTest` with them, for every module that has a `src/androidDeviceTest` directory.
`scripts/instrumented_shard.sh` finds those modules, so a new one joins without editing the
workflow. Their JVM run is not here: `unit-tests` already runs it as part of `jvmTest`.

- **`android`** has two shards, and each boots its own API 35 `x86_64` emulator with KVM. The
  script deals the modules to the shards round-robin, so keep `SHARD_COUNT` equal to the length of
  `matrix.shard`. A shard assembles its test APKs before the emulator boots: that gives Gradle all
  four cores, and a compile error fails the job before the SDK download and the boot. The emulator
  and its system image are installed in their own step, with three attempts, because the emulator
  runner fails the job on the first corrupt download. The tests run with `--max-workers=1`, so one
  module at a time drives the emulator and the log stays readable. The step has a 25-minute
  timeout, under the job's 45: a hung run then still uploads its reports.
- **`ios`** runs the same modules on the iOS simulator of a `macos-latest` runner, which costs
  nothing on a public repository. It caches `~/.konan`, where the Kotlin/Native toolchain lives
  outside the Gradle home, keyed by the Kotlin version alone.

Both upload their test reports when they fail.

## Adding a workflow

- Scope it with `paths` or `paths-ignore`, so a docs-only change doesn't run a build — unless it is
  a required check, which GitHub never reports for a workflow skipped by path filtering.
- Use per-commit `concurrency` groups on `main` if it writes a Gradle cache, per-ref otherwise: a
  cancelled run saves no cache.
- Set up Gradle through `./.github/actions/gradle`, pass `secrets.GRADLE_ENCRYPTION_KEY`, and pick
  one of the two cache switches above if the job is not a default reader.
