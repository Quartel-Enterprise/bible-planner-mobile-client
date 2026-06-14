# Test structure: Given / When / Then

Every test body is split into three sections, each marked with a `// Given`, `// When`, `// Then`
comment and separated by a blank line. (These comments are intentional structure — unlike production
code, where explanatory `//` comments are avoided.)

- **Given** — build the scenario and inputs (usually a single `prepareScenario(...)` call; see
  [prepare-scenario.md](prepare-scenario.md)).
- **When** — exercise the single action under test. Keep it to one behavior per test.
- **Then** — assertions on the resulting state and/or recorded interactions.

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
