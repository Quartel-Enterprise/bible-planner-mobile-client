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

The release workflow writes `local.properties` from the `LOCAL_PROPERTIES` GitHub secret, so add the
`SENTRY_DSN=...` line to that secret as well. Without it, packaged builds produced by CI report nothing.

## Debug vs production

Collection is disabled in development. The `run` and `hotRun` Gradle tasks pass
`-Dbibleplanner.debug=true`, which makes `CrashReporter.configure(isDebug = true)` skip `Sentry.init`.
Anything else — `runDistributable`, `runRelease` and the packaged `.dmg`/`.msi`/`.deb` launchers — has no
such flag and therefore reports normally.

The flag deliberately marks *development* rather than production: if it were ever missed, the failure mode
is extra noise in Sentry, not crash reporting silently switched off in a shipped build.

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
