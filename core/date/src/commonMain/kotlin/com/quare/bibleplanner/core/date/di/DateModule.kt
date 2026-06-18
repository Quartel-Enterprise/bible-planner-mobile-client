package com.quare.bibleplanner.core.date.di

import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProviderImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dateModule = module {
    includes(trustedTimeModule)
    factoryOf(::LocalDateTimeProviderImpl).bind<LocalDateTimeProvider>()
    factoryOf(::GetFinalTimestampAfterEditionUseCase)
}

internal expect val trustedTimeModule: Module
