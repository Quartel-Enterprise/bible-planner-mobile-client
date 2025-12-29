package com.quare.bibleplanner.core.utils.di

import com.quare.bibleplanner.core.utils.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.utils.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.utils.date.LocalDateTimeProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val utilsModule = module {
    factoryOf(::LocalDateTimeProvider)
    factoryOf(::CurrentTimestampProvider)
    factoryOf(::GetFinalTimestampAfterEditionUseCase)
}
