package com.quare.bibleplanner.di

import com.quare.bibleplanner.AppViewModel
import com.quare.bibleplanner.core.provider.koin.commonKoinInitializer
import com.quare.bibleplanner.domain.usecase.InitializeAppContent
import com.quare.bibleplanner.domain.usecase.impl.EnsureStartDateIsAvailableUseCase
import com.quare.bibleplanner.domain.usecase.impl.InitializeAppContentUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.ObserveSelectedVersionUseCase
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

private val appModule = module {
    factoryOf(::EnsureStartDateIsAvailableUseCase)
    factoryOf(::ObserveSelectedVersionUseCase)
    factoryOf(::InitializeAppContentUseCase).bind<InitializeAppContent>()
    viewModelOf(::AppViewModel)
}

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null,
    platformModules: List<Module> = emptyList(),
) {
    commonKoinInitializer(
        platformModules = listOf(appModule) + platformModules,
        config = config,
    )
}
