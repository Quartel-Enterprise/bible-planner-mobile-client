package com.quare.bibleplanner.core.installattribution.di

import com.quare.bibleplanner.core.installattribution.domain.usecase.ReadInstallSource
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformInstallAttributionModule: Module = module {
    factory<ReadInstallSource> { ReadInstallSource { null } }
}
