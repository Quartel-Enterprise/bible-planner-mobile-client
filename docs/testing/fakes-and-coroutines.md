# Fakes & coroutine testing

## Fakes, not mocks

There is no mocking library. Collaborators are faked by hand: a `private class FakeX : X` that records
calls and/or returns canned values, placed at the end of the test file.

For this to be possible, a collaborator must be an interface. When it is a concrete/`final` class (e.g.
the supabase `Auth` / `Realtime` types), introduce a small domain abstraction over it and inject that
instead — prefer a `fun interface` so it can also be faked with a lambda:

```kotlin
fun interface ObserveAuthenticatedUserId {
    operator fun invoke(): Flow<String?>
}
// production: ObserveAuthenticatedUserIdUseCase wraps Auth.sessionStatus
// test:       ObserveAuthenticatedUserId { flowOf("user-1") }
```

A fake only needs real behavior for the methods under test; stub the rest with `error("unused")`.

## Shared fakes

A fake of a `core` interface that more than one module needs lives in that core module's `:testing`
submodule instead of being copied into each test source set:

| Module | Fakes |
| --- | --- |
| `:core:books:testing` | `FakeBooksRepository`, `FakeBibleRepository`, `FakeBibleVersionRepository`, `FakeBibleVersionDownloaderFacade` |
| `:core:plan:testing` | `FakePlanRepository`, `FakeDayRepository` |
| `:core:day_study:testing` | `FakeDayStudyRepository`, `FakeDayStudyGenerationCoordinator` |
| `:core:preferences:theme_selection:testing` | `FakeThemeSelectionRepository` |
| `:core:preferences:material_you:testing` | `FakeMaterialYouRepository` |
| `:core:provider:data_store:testing` | `FakePreferencesDataStore` |
| `:core:provider:room:testing` | `FakeSyncedPreferenceDao`, `FakeBibleVersionDao` |
| `:core:provider:supabase:testing` | `FakeRealtime` |

Depend on it from a test source set only (`commonTest.dependencies { implementation(projects.core.books.testing) }`);
`assertModuleGraph` rejects a production dependency on a `:testing` module, and Kover does not measure
them.

A shared fake is not written for one test, so it behaves like an in-memory version of the real
thing instead of stubbing methods with `error("unused")`: its state is a public `MutableStateFlow` a
test can set or read (`books`, `startDate`, `day`), every write is recorded in a public list
(`favoriteUpdates`, `readStatusUpdates`, `notesUpdates`) and applied to that state, and knobs for
failure paths are public `var`s (`statusError`, `eventsError`, `statusGate`). Its state never
completes, so a use case that collects it forever runs in `backgroundScope` (see below). When a test
needs one method to behave differently, delegate to the shared fake and override just that method:

```kotlin
object : BibleRepository by FakeBibleRepository(bibles = emptyList(), selectedVersionId = versionId) {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = MutableSharedFlow()
}
```

A fake used by a single module stays a `private class` in that module's tests.

## Coroutines

- Wrap test bodies in `runTest { }`. Virtual time auto-advances, so timeouts/`delay` resolve without
  real waiting.
- **ViewModels** (which use `viewModelScope` / `Dispatchers.Main`): set the main dispatcher in
  `@BeforeTest` and reset it in `@AfterTest`, and opt in to the experimental test API:

  ```kotlin
  @OptIn(ExperimentalCoroutinesApi::class)
  internal class XViewModelTest {
      private val testDispatcher = UnconfinedTestDispatcher()
      @BeforeTest fun setUp() = Dispatchers.setMain(testDispatcher)
      @AfterTest fun tearDown() = Dispatchers.resetMain()
      @Test fun x() = runTest(testDispatcher) { ... }
  }
  ```

- **Long-running collectors** (a manager that collects forever, a `SharedFlow` of UI actions): launch
  them with `backgroundScope.launch { ... }` (auto-cancelled at test end) and step with `runCurrent()`.
  Collect a `SharedFlow` into a list via `backgroundScope` *before* triggering the action — it has no
  replay buffer.

## Gotcha: exception identity

`kotlinx-coroutines` stacktrace recovery may **copy** an exception as it crosses coroutine boundaries,
so the caught instance is not the same object you threw. Assert the exception **type and message**, not
identity:

```kotlin
val thrown = useCase().exceptionOrNull()
assertIs<IllegalStateException>(thrown)
assertEquals("boom", thrown.message)
```

## Misc

- `Duration` constants are `private val` in the test class, not in a `companion object` (same code-style
  rule as production).
