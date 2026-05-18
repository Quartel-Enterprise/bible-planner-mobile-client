package com.quare.bibleplanner.feature.applanguage.di

import com.quare.bibleplanner.feature.applanguage.data.mapper.AppLanguageMapper
import com.quare.bibleplanner.feature.applanguage.data.mapper.AppLanguageMapperImpl
import com.quare.bibleplanner.feature.applanguage.data.repository.AppLanguageRepositoryImpl
import com.quare.bibleplanner.feature.applanguage.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveAppLocale
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetAppLanguage
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.GetAppLanguageFlowUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.ObserveAppLocaleUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.SetAppLanguageUseCase
import com.quare.bibleplanner.feature.applanguage.presentation.AppLanguageViewModel
import com.quare.bibleplanner.feature.applanguage.presentation.factory.AppLanguageUiStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appLanguageModule = module {
    // Data
    factoryOf(::AppLanguageRepositoryImpl).bind<AppLanguageRepository>()
    factoryOf(::AppLanguageMapperImpl).bind<AppLanguageMapper>()

    // Domain
    factoryOf(::GetAppLanguageFlowUseCase).bind<GetAppLanguageFlow>()
    factoryOf(::SetAppLanguageUseCase).bind<SetAppLanguage>()
    factoryOf(::ObserveAppLocaleUseCase).bind<ObserveAppLocale>()

    // Presentation
    factoryOf(::AppLanguageUiStateFactory)
    viewModelOf(::AppLanguageViewModel)
}
