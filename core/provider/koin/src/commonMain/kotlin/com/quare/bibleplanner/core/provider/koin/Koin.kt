package com.quare.bibleplanner.core.provider.koin

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null,
    platformModules: List<Module> = emptyList(),
) {
    startKoin {
        config?.invoke(this)
        modules(CommonKoinUtils.modules + platformModules)
    }
}
