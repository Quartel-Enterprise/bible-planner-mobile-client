package com.quare.bibleplanner.core.date.di

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dateModule = module {
    factoryOf(::LocalDateTimeProvider)
    factoryOf(::CurrentTimestampProvider)
    factoryOf(::GetFinalTimestampAfterEditionUseCase)
}

