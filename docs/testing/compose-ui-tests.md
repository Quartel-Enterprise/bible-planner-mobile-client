# Compose UI tests

Screens are tested with Compose Multiplatform's
[`runComposeUiTest`](https://kotlinlang.org/docs/multiplatform/compose-test.html), the API JetBrains
recommends for multiplatform UI tests. A test is written once, in `commonTest`, and runs on:

| Target | Task | Where it runs |
| --- | --- | --- |
| Desktop JVM | `jvmTest` | every pull request, in `build-and-test`, and it counts towards coverage |
| iOS simulator | `iosSimulatorArm64Test` | the `ios` job of `instrumented-tests` |
| Android device | `connectedAndroidDeviceTest` | the `android` job of `instrumented-tests`, on an API 35 emulator |

The Android host (`testAndroidHostTest`) has no `Instrumentation` to launch the test activity with, so
`build-logic` excludes the `*UiTest` classes from it. The rest of `commonTest` still runs there.

## Writing one

- One class per screen, in `src/commonTest`, in the package of the composable it renders. Its name
  **must end in `UiTest`**: that suffix is what keeps it off the Android host.
- Render the stateless content composable — the one that takes `uiState` and `onEvent`, never the
  `*Root` wired to a ViewModel — with a fixed `UiState`. Then assert on text, content descriptions
  and test tags, and on the `UiEvent`s it sends to `onEvent`.
- Use `androidx.compose.ui.test.v2.runComposeUiTest`. The `androidx.compose.ui.test` one is
  deprecated.
- Set the content with `setUiTestContent { }` from `:ui:testing`, not `setContent`. It adds the
  composition locals that a platform's test host leaves out: on iOS, the `LocalUIViewController`
  that Calf's native switches, menus and pickers read as soon as they are composed.
- Resolve every expected string with `getString(Res.string.…)`. The tests run in the machine's
  locale, so a hardcoded English string fails on a Portuguese one. The block of `runComposeUiTest`
  is a suspend function, so it can call `getString` directly.
- The usual structure still applies: Given / When / Then, and a `ComposeUiTest.prepareScenario(...)`
  as the last member. It sets the content and assigns the `lateinit` fields the test reads, such as
  the list of emitted events. A render-only test uses `waitForIdle()` as its **When**.
- Calf renders menus, pickers and alerts natively on iOS, as a `UIMenu` or a `UIAlertController`,
  outside the Compose semantics tree. On the iOS simulator a test can open one of them, but it
  can't find or click the items inside it. Test the button that opens a menu, not the items in it:
  the ViewModel tests already cover the event an item sends.
- A screen that also has store screenshots shares their sample data. The fixture lives in
  `commonTest/.../fixture`, which the screenshots in `androidHostTest` also see.

```kotlin
@OptIn(ExperimentalTestApi::class)
internal class ReadingPlanUiTest {
    private lateinit var events: MutableList<ReadingPlanUiEvent>

    @Test
    fun `GIVEN a loaded plan WHEN clicking more options THEN emits OnOverflowClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = readingPlanUiState())

        // When
        onNodeWithContentDescription(getString(Res.string.more_options)).performClick()

        // Then
        assertEquals(expected = listOf<ReadingPlanUiEvent>(ReadingPlanUiEvent.OnOverflowClick), actual = events)
    }

    private fun ComposeUiTest.prepareScenario(uiState: ReadingPlanUiState) {
        events = mutableListOf()
        setUiTestContent {
            ReadingPlanScreen(
                uiState = uiState,
                onEvent = { event -> events += event },
                // ...
            )
        }
    }
}
```

## Running them

```bash
./gradlew :feature:reading_plan:jvmTest --tests '*UiTest'
```

```bash
./gradlew :feature:reading_plan:iosSimulatorArm64Test --tests '*UiTest'
```

```bash
./gradlew :feature:reading_plan:connectedAndroidDeviceTest
```

`scripts/instrumented_shard.sh` prints those tasks for every module that has device tests, which is
what CI runs:

```bash
./gradlew $(./scripts/instrumented_shard.sh connected 0 1)
```

## On an Android device

A module runs on a device once it has `src/androidDeviceTest/AndroidManifest.xml`. `build-logic`
(`ComposeUiTests.kt`) then does the rest:

- It puts `androidDeviceTest` in the `test` source set tree, so it depends on `commonTest` the way
  `jvmTest` and `iosTest` do. That means **the whole `commonTest` runs on the device**, and not only
  the UI tests.
- It sets `AndroidJUnitRunner` and adds `ui-test-junit4-android` and `ui-test-manifest`. It also pins
  Espresso to 3.7: the version Compose pulls in calls `InputManager.getInstance`, which Android 16
  removed, and every test fails on a recent device.
- It raises the module's `minSdk` to 30, only in a build that asks for a task ending in
  `AndroidDeviceTest`. The test names are backticked sentences, and D8 accepts a space in a method
  name only from API 30's DEX format on. AGP has no setting for the test APK's own level, and it
  ignores the level that Android Studio injects for the target device. The app, which still supports
  API 26, never builds against the raised value.

So:

- Run the device tests through `connectedAndroidDeviceTest` or `assembleAndroidDeviceTest`.
  `connectedCheck` and `connectedAndroidTest` don't raise the level and fail at dexing.
- Use an API 30+ device or emulator.
- Keep test names to letters, digits, spaces, `-` and `_`. D8 rejects an apostrophe or a comma at any
  API level.

## What is covered

| Screen | Test |
| --- | --- |
| Reading plan | `feature/reading_plan/.../presentation/content/ReadingPlanUiTest.kt` |
| Day | `feature/day/.../presentation/DayUiTest.kt` |
| Day study | `feature/day_study/.../presentation/DayStudyUiTest.kt` |
| Books | `feature/books/.../presentation/BooksUiTest.kt` |
| Book details | `feature/book_details/.../presentation/BookDetailsUiTest.kt` |
| Read | `feature/read/.../presentation/screen/ReadUiTest.kt` |
| Profile (preferences and account) | `feature/profile/.../presentation/ProfileUiTest.kt` |
