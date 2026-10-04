# Test structure: Given / When / Then

Every test body is split into three sections, each marked with a `// Given`, `// When`, `// Then`
comment and separated by a blank line. (These comments are intentional structure — unlike production
code, where explanatory `//` comments are avoided.)

- **Given** — build the scenario and inputs (usually a single `prepareScenario(...)` call; see
  [prepare-scenario.md](prepare-scenario.md)).
- **When** — exercise the single action under test. Keep it to one behavior per test.
- **Then** — assertions on the resulting state and/or recorded interactions.

Each marker is exactly `// Given`, `// When` or `// Then`, alone on its line, and each appears once, in that order.
A note about a section goes on its own comment line below the marker. Every section holds code: when the system
under test is a property of the class, the inputs still go in the **Given** as named `val`s rather than inline in
the **When**.

The one exception is a class that builds its scenario in a `@BeforeTest` function (a `prepareScenario` with no
parameters becomes one): its tests may leave out `// Given`, since there is nothing left to arrange.

A test that would need two **When**s checks two behaviours: split it, or move the steps that only build the state
into the **Given**. An expected failure is captured in the **When** and asserted in the **Then**:

```kotlin
// When
val result = runCatching { mapper.parse(label) }

// Then
assertIs<IllegalArgumentException>(result.exceptionOrNull())
```

## Naming

Test names are backticked, spaced sentences that **mirror the three sections**, in the form
`` `GIVEN <state> WHEN <action> THEN <outcome>` ``. The `GIVEN`/`WHEN`/`THEN` keywords are **uppercase**
in the name (so they stand out as structure, not prose) and line up with the `// Given` / `// When` /
`// Then` blocks of the body, so the name reads as a summary of the test and the body reads as its
expansion.

```
GIVEN a flush failure WHEN confirming THEN moves to pending changes error without a snackbar
└──── GIVEN ────┘ └─ WHEN ─┘ └──────────────────── THEN ─────────────────────────┘
```

The name holds only letters, digits, spaces, `-` and `_`: tests are dexed for the Android device tests, and D8
rejects any other character in a method name ("the reading of today", not "today's reading").

## Example

```kotlin
@Test
fun `GIVEN a successful logout WHEN confirming THEN emits NavigateBack`() = runTest(testDispatcher) {
    // Given
    prepareScenario(result = Result.success(Unit))

    // When
    viewModel.onEvent(LogoutUiEvent.OnConfirmLogout)

    // Then
    assertEquals(listOf(LogoutUiAction.NavigateBack), actions)
}
```

## Enforcement

Two custom ktlint rules (in `tools/ktlint-custom-rules`) check every test source set:

- `bible-planner-style:test-name-given-when-then` — an `@Test` name matches `GIVEN … WHEN … THEN …` and holds
  only the characters above.
- `bible-planner-style:test-body-sections` — the body has `// Given`, `// When` and `// Then`, once each and in
  that order (`// Given` optional in a class with a `@BeforeTest`).

[Screenshot tests](screenshot-tests.md) and the store screenshot generators (every file under a `screenshots`
package of a test source set) are exempt: a test there is one render, named after its image. The exemptions are
sections of `.editorconfig`. Whether a name really describes its body, or a test checks a single behaviour, is
for the review.
