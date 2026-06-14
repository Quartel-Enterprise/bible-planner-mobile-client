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
