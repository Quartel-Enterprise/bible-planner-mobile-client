package com.quare.bibleplanner.core.installattribution.di

import com.quare.bibleplanner.core.installattribution.data.AndroidInstallSourceReader
import com.quare.bibleplanner.core.installattribution.domain.usecase.ReadInstallSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformInstallAttributionModule: Module = module {
    factory { AndroidInstallSourceReader(androidContext()) }
    factory<ReadInstallSource> {
        val reader = get<AndroidInstallSourceReader>()
        ReadInstallSource { reader.read() }
    }
}
