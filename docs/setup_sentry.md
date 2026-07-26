# Sentry Setup Guide (Desktop Crash Reporting)

The desktop (JVM) target reports crashes to [Sentry](https://sentry.io) instead of Firebase Crashlytics,
because Crashlytics has no JVM SDK and no public ingestion API — it only ships SDKs for Android, Apple,
Flutter, Unity and C++, and its REST API is read-only (`list`/`batchGet`).

Android and iOS keep using Crashlytics. Only the `jvmMain` `CrashReporter` implementation talks to Sentry.

## 1. Create a Sentry project

1. Log in to [Sentry](https://sentry.io) (the free tier covers 5k errors/month).
2. Create a new project and pick **Java** as the platform.
3. Copy the **DSN** — it looks like `https://<publicKey>@<org>.ingest.sentry.io/<projectId>`.

## 2. Configure local.properties

`local.properties` holds sensitive configuration and is not checked into version control.

1. Open `local.properties` in the root of the project.
2. Add the following line:

```properties
SENTRY_DSN=your_dsn_here
```

3. Save the file.

Leaving the key out (or blank) is supported: crash reporting is simply disabled and the build prints
`⚠️ SENTRY_DSN not found in local.properties. Desktop crash reporting will be disabled.`

## 3. Sync Project

After updating `local.properties`, sync Gradle to regenerate `CrashlyticsBuildKonfig`.

- **Android Studio / IntelliJ**: Click "Sync Project with Gradle Files" (Elephant icon).
- **Command Line**: Run `./gradlew clean build`.

## 4. Configure CI

`SENTRY_DSN` is its **own** secret on the `Production` environment, alongside the other build secrets — it
does not live inside the `LOCAL_PROPERTIES` blob. The release workflow appends it to `local.properties`
right after writing that blob:

```bash
gh secret set SENTRY_DSN --env Production --repo Quartel-Enterprise/bible-planner-mobile-client
```

Keeping it separate is what makes rotation cheap. GitHub secrets are write-only — the current value cannot
be read back — so folding the DSN into `LOCAL_PROPERTIES` would mean reconstructing every key in that blob
from scratch on every change, with no way to diff against what is already there.

If the secret is unset, the workflow appends a blank value, which disables reporting instead of failing the
build.

## Debug vs production

Collection is enabled only when the app runs from a packaged distribution. `Main.kt` treats the presence
of the `jpackage.app-path` system property as the signal: the jpackage launcher inside the
`.dmg`/`.msi`/`.deb` sets it to its own executable path, and nothing else does.

So the packaged app (and `runDistributable`, which launches it) reports, while **every** development entry
point stays silent — `./gradlew run`, `hotRun`, the IDE's Run button, and test tasks alike. Keying off the
artifact rather than off specific Gradle tasks is what makes the IDE case work: a run started from the IDE
never goes through the Gradle `run` task, so a task-based flag would have left it reporting to production.

This property is not mentioned in the jpackage documentation, so it was verified empirically against a real
`createDistributable` build: the packaged launcher reports the path, `./gradlew run` reports `null`.

## What gets reported

- Uncaught exceptions, via Sentry's default `UncaughtExceptionHandler` integration (`handled: false`).
- Every `Logger.e(throwable)` call site, via the existing kermit `CrashReporterLogWriter` bridge — the same
  mechanism that feeds non-fatals to Crashlytics on mobile.
- Events are tagged with `release = com.quare.bibleplanner@<versionName>`, which drives Sentry's release
  health and crash-free metrics.

`isAttachServerName` is disabled so the user's machine hostname is never sent.

## Switching backends

Sentry's SDK protocol is also spoken by [GlitchTip](https://glitchtip.com/), an open-source,
Sentry-API-compatible backend. Moving to it (self-hosted or their cloud) only requires pointing
`SENTRY_DSN` at the other server — no code changes.

## Troubleshooting

- **Nothing arrives in Sentry**: confirm the DSN is set, that you are not running via `run`/`hotRun`, and
  re-sync Gradle so `CrashlyticsBuildKonfig` picks the value up.
- **Verifying without a Sentry account**: a DSN is just `http://<anyKey>@<host>:<port>/<projectId>`, so you
  can point it at a local HTTP server and inspect the posted envelopes.
