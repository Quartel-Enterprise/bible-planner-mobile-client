package com.quare.bibleplanner.core.provider.language.di

import com.quare.bibleplanner.core.provider.language.data.mapper.AppLanguageMapper
import com.quare.bibleplanner.core.provider.language.data.repository.AppLanguageRepositoryImpl
import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlowUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val languageProviderModule = module {
    factoryOf(::AppLanguageMapper)
    factoryOf(::AppLanguageRepositoryImpl).bind<AppLanguageRepository>()
    factoryOf(::GetAppLanguageFlowUseCase).bind<GetAppLanguageFlow>()
}
