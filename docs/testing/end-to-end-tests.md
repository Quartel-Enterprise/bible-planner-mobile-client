# End-to-end flows

The end-to-end flows drive the whole app from launch, the way a user would: the real navigation,
ViewModels, use cases, Room and DataStore, with only the outside world replaced. They are Compose UI
tests too, written with `runComposeUiTest` (JetBrains has no separate end-to-end tool for Compose
Multiplatform), and they live in `shared/src/commonTest/kotlin/com/quare/bibleplanner/e2e`.

| Flow | Test |
| --- | --- |
| Plan → day → reading: read the first day's chapters, then see the day complete and the plan progress move | `PlanReadingFlowUiTest` |
| Books → chapter: open a book, read a chapter, see it counted in the book's progress | `BookChapterFlowUiTest` |
| Verse note: save a note on a verse, reopen the chapter, see it again; the free-notes limit offers Pro | `VerseNoteFlowUiTest` |
| Preferences: the theme darkens the screen, the language translates it, a Bible version is downloaded and shown | `PreferencesFlowUiTest` |

Each flow runs in portrait and in a wide window: 400×720 and 1000×720 dp on the JVM, the phone in
portrait and in landscape on Android. The wide window is past the 700 dp at which the app switches to
its two-pane layouts.

## What is real and what is fake

`E2eApp` starts Koin with `initializeKoin`, the app's own entry point, plus `e2eKoinModule`, which
overrides what reaches outside the process:

| Real | Fake |
| --- | --- |
| Room, in memory, a new database per test | the Supabase client: a `MockEngine` (`FakeSupabaseServer`) that serves two Bible versions from Storage and answers every other call as it would a signed-out user |
| DataStore, on a file in a new temporary directory per test | billing: a free user, no store (`Purchases` is an expect class, so the domain interfaces are faked) |
| the bundled books and reading plans | remote config (every key at its default), analytics and crash reporting (no-ops) |
| every use case, repository and ViewModel, and the Bible download pipeline | the clock, fixed on 2 March 2026, so the plan starts that day and day 1 is today |
| | connectivity (always online) and the download notifier (silent) |

The Bible text is not bundled: the app downloads it from Supabase Storage. `FakeSupabaseServer`
serves every chapter of two English versions, `WEB` and `KJV`, with verses that read like
`WEB Gn 1:3`, so a test can tell which version and chapter is on screen.

Before the app is shown, `E2eApp` does what a first launch does: it seeds the books, lists the Bible
versions, sets the language to English and downloads `WEB`. A test adds its own state in the
`arrange` block of `launch`, through the app's use cases, such as the day notes that put a free user
at the notes limit.

## A clean state for every flow

Each test gets a new Koin graph, a new in-memory database and a new DataStore file, and `stop()`
clears the ViewModels, cancels the app's own scopes and stops Koin. The database is left open on
purpose: a ViewModel that is still being cancelled may be halfway through a query, and closing the
database under it fails the next test with an uncaught exception.

What outlives a test is put back by the platform:

- **JVM**: the desktop app applies its language with `Locale.setDefault`, so every test starts in
  English and restores the machine's locale at the end.
- **Android**: the flows launch the app's own `MainActivity` (declared in
  `src/androidDeviceTest/AndroidManifest.xml`), because only that activity reads the language back
  after a change recreates it. It reads it from `app_prefs`, which the test sets to English and
  clears at the end.

That is why no test orchestrator with `clearPackageData` is needed: nothing a flow writes is left on
disk for the next one.

## Writing a flow

- Name the class `*FlowUiTest`. The `UiTest` suffix is what keeps it off the Android host and what
  `-PuiTests` selects, like any other [Compose UI test](compose-ui-tests.md).
- Given / When / Then as usual. The **Given** calls `prepareScenario`, which launches the app.
- The app reads Room and DataStore off the main thread, which `waitForIdle` doesn't wait for. Find
  every node through the helpers in `E2eInteractions.kt`: `awaitText`, `awaitNode`, `clickText`,
  `clickDescription` and `click` wait for the node, and the click helpers scroll it into view first,
  since a phone in landscape leaves most lists below the fold. When a node doesn't show up, the error
  lists every text on screen.
- Assert what the user sees. A matcher that waits for a state (`isOn()`, a text that only appears
  afterwards) is better than asserting right after a click.
- The screens' strings live in each feature module's `Res`, which is internal, so the flows use the
  English text. The app is always in English when a flow starts.
- Colours are checked through the screen's average luminance (`awaitLightScreen`,
  `awaitDarkScreen`), which holds on any screen and in both windows.

## Running them

```bash
./gradlew :shared:jvmTest -PuiTests=only
```

```bash
./gradlew :shared:connectedAndroidDeviceTest
```

On a task emulator, install `shared/build/outputs/apk/androidTest/shared-androidTest.apk` and run
`com.quare.bibleplanner.shared.test/androidx.test.runner.AndroidJUnitRunner`, as
[task-emulator.md](../../.claude/skills/task-emulator.md) describes.

`:shared` has a `src/androidDeviceTest` directory, so `scripts/ui_test_shard.sh` puts it in the
`desktop` and `android` jobs of `ui-tests` like every screen module. It leaves it out of the `ios`
job, and the build filters the flows out of `iosSimulatorArm64Test`: they switch tabs, and on iOS the
main tabs are Calf's native `UITabBar`, outside the Compose semantics tree. They still compile for
iOS, so the iOS test source set has its own `E2ePlatform`.
