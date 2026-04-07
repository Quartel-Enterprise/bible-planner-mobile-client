package com.quare.bibleplanner.core.provider.koin

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun commonKoinInitializer(
    platformModules: List<Module>,
    config: (KoinApplication.() -> Unit)? = null,
) {
    startKoin {
        config?.invoke(this)
        modules(CommonKoinUtils.modules + platformModules)
    }
}
