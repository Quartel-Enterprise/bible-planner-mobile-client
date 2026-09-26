# Testing guide

Conventions for automated tests in this project. Follow these when adding or changing tests.

- [Test structure: Given / When / Then](given-when-then.md)
- [The `prepareScenario` factory](prepare-scenario.md)
- [Fakes & coroutine testing](fakes-and-coroutines.md)
- [Compose UI tests](compose-ui-tests.md)
- [End-to-end flows](end-to-end-tests.md)

## At a glance

- Tests live in each module's `src/commonTest` and run on the JVM/desktop target (`:module:jvmTest` / `:module:desktopTest`) and on the Android host (`:module:testAndroidHostTest`).
- A module with a `src/commonTest` directory declares `withHostTest {}` in its `android {}` block; otherwise AGP warns on every build that the directory exists but android host tests are not enabled.
- A test that needs the desktop JVM runtime goes in `src/jvmTest` instead: e.g. Room migration specs (the bundled SQLite driver has no native library on the Android host) or anything calling compose-resources `getString()` (it reads Android `Resources`, which are not mocked on the host).
- Compose UI tests are `commonTest` classes named `*UiTest`, written with `runComposeUiTest`. They run on the JVM (`jvmTest` runs them with the other tests unless `-PuiTests` picks one kind), on the iOS simulator, and on an Android device in the modules that have a `src/androidDeviceTest` directory — never on the Android host. See [Compose UI tests](compose-ui-tests.md).
- CI runs `./gradlew jvmTest -PuiTests=exclude` on every pull request, together with the coverage rules: 80% of the lines, for the merged report and for each new file — see [docs/code-quality.md](../code-quality.md#test-coverage). The `ui-tests` workflow runs the UI tests on the JVM, on an emulator and on the iOS simulator — see [docs/ci.md](../ci.md#ui-tests).
- Stack: `kotlin.test` + `kotlinx-coroutines-test`, and Compose's multiplatform `ui-test` for the UI tests. No mocking library — collaborators are faked by hand.
- Every test body is split into **Given / When / Then** sections.
- Each test class ends with a single `prepareScenario(...)` factory that assembles the system under test and its fakes, so tests don't repeat instantiation.
- Test names may only hold letters, digits, spaces, `-` and `_`: in a module with device tests they are dexed for Android, and D8 rejects an apostrophe or a comma ("the reading of today", not "today's reading").

## What is covered where

| Level | What it exercises | Where it lives | Runs on | CI job |
| --- | --- | --- | --- | --- |
| Unit tests | a use case, repository, mapper or ViewModel, with its collaborators faked by hand | each module's `src/commonTest` (`src/jvmTest` when it needs the desktop JVM) | JVM, Android host | `unit-tests` in `build-and-test`, with the 80% coverage rules |
| Compose UI tests | one screen's stateless content, with a fixed `UiState` | `*UiTest` classes in the screen's module `src/commonTest` | JVM, Android device, iOS simulator | `ui-tests` (`desktop`, `android`, `ios`) |
| End-to-end flows | the whole app from launch, through the real navigation, ViewModels, Room and DataStore, with only the outside world faked | `shared/src/commonTest/.../e2e` | JVM, Android device | `ui-tests` (`desktop`, `android`) |

A flow that crosses screens belongs in the end-to-end flows. What one screen shows for a given state,
and the events it sends, belongs in its UI test, and every branch of the logic behind it in the unit
tests: they are much cheaper to run and to read when they fail.
