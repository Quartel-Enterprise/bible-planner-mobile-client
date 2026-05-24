package com.quare.bibleplanner.core.date.di

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.CurrentTimestampProviderImpl
import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProviderImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dateModule = module {
    factoryOf(::LocalDateTimeProviderImpl).bind<LocalDateTimeProvider>()
    factoryOf(::CurrentTimestampProviderImpl).bind<CurrentTimestampProvider>()
    factoryOf(::GetFinalTimestampAfterEditionUseCase)
}
