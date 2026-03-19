## Use Cases

### Pattern A — `fun interface` (preferred for injectable abstractions)

```kotlin
// domain/usecase/GetSomethingFlow.kt
fun interface GetSomethingFlow {
    operator fun invoke(): Flow<Something>
}

// domain/usecase/impl/GetSomethingFlowUseCase.kt
class GetSomethingFlowUseCase(
    private val repository: SomeRepository,
) : GetSomethingFlow {
    override fun invoke(): Flow<Something> = repository.getSomethingFlow()
}
```

Koin binding:
```kotlin
factoryOf(::GetSomethingFlowUseCase).bind<GetSomethingFlow>()
```

### Pattern B — concrete class (for complex use cases that don't need an abstraction)

```kotlin
class MarkChapterAsReadUseCase(
    private val repository: DayRepository,
) {
    suspend operator fun invoke(chapterNumber: Int) {
        repository.markChapterRead(chapterNumber)
    }
}
```

### Use case aggregation (when a ViewModel needs many use cases)

```kotlin
// domain/usecase/DayUseCases.kt
data class DayUseCases(
    val getSomethingFlow: GetSomethingFlow,
    val markChapterAsRead: MarkChapterAsReadUseCase,
    val anotherUseCase: AnotherUseCase,
)
```

Koin:
```kotlin
factoryOf(::DayUseCases)
viewModelOf(::DayViewModel)
```

## Repository Pattern

```kotlin
// domain/repository/SomeRepository.kt
interface SomeRepository {
    fun getSomethingFlow(): Flow<Something>
    suspend fun doAction(param: Type)
}

// data/repository/SomeRepositoryImpl.kt
class SomeRepositoryImpl(
    private val dao: SomeDao,
    private val mapper: SomeMapper,
) : SomeRepository {
    override fun getSomethingFlow(): Flow<Something> =
        dao.getAllFlow().map(mapper::map)

    override suspend fun doAction(param: Type) {
        dao.insert(mapper.toEntity(param))
    }
}
```
