# Getting started

## Prerequisites

- JDK 21 or higher
- Android Studio or IntelliJ IDEA
- Android SDK, for Android development
- Xcode (macOS only), for iOS development

## Local configuration

Three files the app needs are kept out of the repository because they carry credentials. Ask a
maintainer for them before the first build:

| File | What it carries |
|---|---|
| `local.properties` | The Android SDK path plus every secret the app reads: Supabase, RevenueCat, the GA4 desktop stream, the Sentry DSN and the donation keys. |
| `androidApp/google-services.json` | Firebase for Android — Crashlytics, Analytics and Remote Config. |
| `iosApp/iosApp/GoogleService-Info.plist` | The same, for iOS. |

The keys in `local.properties` reach the code through BuildKonfig. A missing one is a warning at
configuration time, not an error: it compiles to an empty string, and the feature that needs it
fails at runtime instead of failing the build.

## Build and run the Android app

Use the run configuration in your IDE's run widget, or build it from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

## Build and run the desktop (JVM) app

Use the run configuration in your IDE's run widget, or run it from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :desktopApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :desktopApp:run
  ```

## Build and run the iOS app

Use the run configuration in your IDE's run widget, or open [/iosApp](../iosApp) in Xcode and run it
from there.

## Next steps

- [Architecture guide](ai_agents.md) — the conventions every module follows, and the checklist a new
  feature goes through.
- [Code quality](code-quality.md) — the ktlint setup, and how to run exactly what CI runs.
- [Testing](testing/README.md) — what is tested, and how the suites are organised.
