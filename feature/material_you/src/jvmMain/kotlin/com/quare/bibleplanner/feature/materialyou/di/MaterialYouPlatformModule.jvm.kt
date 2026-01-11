package com.quare.bibleplanner.feature.materialyou.di

import com.quare.bibleplanner.feature.materialyou.domain.usecase.IsDynamicColorSupported
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val MaterialYouPlatformModule: Module = module {
    factory {
        IsDynamicColorSupported { false }
    }
}
