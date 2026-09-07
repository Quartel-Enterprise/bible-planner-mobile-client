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

### Register your debug SHA-1, or Remote Config stays silent (Android)

The Firebase Android API key is restricted to a list of package name + SHA-1 certificate fingerprint
pairs. A debug build signed with a keystore that is not on that list is rejected by Firebase
Installations, and without an installation token **Remote Config never fetches**.

Nothing crashes when that happens, which is what makes it hard to spot. Every key falls back to its
in-app default — `false` for booleans — so the app is merely, quietly wrong: the profile screen
loses the donation card and the Instagram link, and its first section reads "Pro" instead of
"Pro & Support". It reads like intended behaviour rather than a broken setup.

Two warnings in logcat right after launch confirm it:

```text
W/Firebase-Installations       "reason": "API_KEY_ANDROID_APP_BLOCKED"
W/AndroidRemoteConfigService   Failed to fetch the remote config (last fetch status: FAILURE). […]
```

To get unblocked, print your fingerprint and take the `SHA1` line of the `debug` variant:

```shell
./gradlew :androidApp:signingReport
```

Then ask a maintainer to add it, with the app's package name, to the Android restrictions of the
Firebase project's Android API key (Google Cloud console → APIs & Services → Credentials). This
needs access to the Cloud project, so you cannot do it yourself, and it takes up to five minutes to
propagate. Every developer needs their own machine's fingerprint on that list.

To verify afterwards, relaunch the app: both warnings disappear, and the config the device actually
activated lands in `files/frc_<app id>_firebase_activate.json` inside the app's private storage
(`adb shell run-as <package> ls files/`).

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
