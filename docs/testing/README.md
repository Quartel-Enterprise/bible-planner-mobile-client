# Testing guide

Conventions for automated tests in this project. Follow these when adding or changing tests.

- [Test structure: Given / When / Then](given-when-then.md)
- [The `prepareScenario` factory](prepare-scenario.md)
- [Fakes & coroutine testing](fakes-and-coroutines.md)

## At a glance

- Tests live in each module's `src/commonTest` and run on the JVM/desktop target (`:module:jvmTest` / `:module:desktopTest`) and on the Android host (`:module:testAndroidHostTest`).
- A module with a `src/commonTest` directory declares `withHostTest {}` in its `android {}` block; otherwise AGP warns on every build that the directory exists but android host tests are not enabled.
- A test that needs the desktop JVM runtime goes in `src/jvmTest` instead: e.g. Room migration specs (the bundled SQLite driver has no native library on the Android host) or anything calling compose-resources `getString()` (it reads Android `Resources`, which are not mocked on the host).
- Stack: `kotlin.test` + `kotlinx-coroutines-test`. No mocking library — collaborators are faked by hand.
- Every test body is split into **Given / When / Then** sections.
- Each test class ends with a single `prepareScenario(...)` factory that assembles the system under test and its fakes, so tests don't repeat instantiation.
