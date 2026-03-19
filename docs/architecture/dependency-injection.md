## Dependency Injection (Koin)

Every module has exactly one file in `di/`:

```kotlin
// di/someFeatureModule.kt
val someFeatureModule = module {
    singleOf(::SomeRepositoryImpl).bind<SomeRepository>()
    factoryOf(::GetSomethingFlowUseCase).bind<GetSomethingFlow>()
    factoryOf(::AnotherUseCaseImpl)
    factoryOf(::SomeUseCases)
    viewModelOf(::SomeViewModel)
}
```

**Register the new module** in `core/provider/koin/src/commonMain/.../CommonKoinUtils.kt`:

```kotlin
val modules = listOf(
    // ... existing modules
    someFeatureModule,
)
```
