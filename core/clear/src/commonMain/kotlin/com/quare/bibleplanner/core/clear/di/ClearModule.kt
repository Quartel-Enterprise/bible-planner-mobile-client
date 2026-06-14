package com.quare.bibleplanner.core.clear.di

import com.quare.bibleplanner.core.clear.domain.ClearLocalUserData
import com.quare.bibleplanner.core.clear.domain.ClearLocalUserDataUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clearModule = module {
    factoryOf(::ClearLocalUserDataUseCase).bind<ClearLocalUserData>()
}
