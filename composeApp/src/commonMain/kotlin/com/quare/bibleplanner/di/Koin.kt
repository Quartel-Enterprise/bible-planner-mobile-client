package com.quare.bibleplanner.di

import com.quare.bibleplanner.AppViewModel
import com.quare.bibleplanner.core.provider.koin.commonKoinInitializer
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val appModule = module {
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
