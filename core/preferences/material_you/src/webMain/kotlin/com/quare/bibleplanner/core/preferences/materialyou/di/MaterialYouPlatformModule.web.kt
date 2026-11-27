package com.quare.bibleplanner.core.preferences.materialyou.di

import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.IsDynamicColorSupported
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val MaterialYouPlatformModule: Module = module {
    factory {
        IsDynamicColorSupported { false }
    }
}
