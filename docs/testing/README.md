# Testing guide

Conventions for automated tests in this project. Follow these when adding or changing tests.

- [Test structure: Given / When / Then](given-when-then.md)
- [The `prepareScenario` factory](prepare-scenario.md)
- [Fakes & coroutine testing](fakes-and-coroutines.md)

## At a glance

- Tests live in each module's `src/commonTest` and run on the JVM/desktop target (`:module:jvmTest` / `:module:desktopTest`).
- Stack: `kotlin.test` + `kotlinx-coroutines-test`. No mocking library — collaborators are faked by hand.
- Every test body is split into **Given / When / Then** sections.
- Each test class ends with a single `prepareScenario(...)` factory that assembles the system under test and its fakes, so tests don't repeat instantiation.
