package com.quare.bibleplanner.feature.applanguage.di

import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetLanguageSyncEnabledFlow
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveAppLocale
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveLanguageSync
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetAppLanguage
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetLanguageSyncEnabled
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.GetLanguageSyncEnabledFlowUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.ObserveAppLocaleUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.ObserveLanguageSyncUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.SetAppLanguageUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.SetLanguageSyncEnabledUseCase
import com.quare.bibleplanner.feature.applanguage.presentation.AppLanguageViewModel
import com.quare.bibleplanner.feature.applanguage.presentation.factory.AppLanguageUiStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appLanguageModule = module {
    // Domain
    factoryOf(::SetAppLanguageUseCase).bind<SetAppLanguage>()
    factoryOf(::ObserveAppLocaleUseCase).bind<ObserveAppLocale>()
    factoryOf(::GetLanguageSyncEnabledFlowUseCase).bind<GetLanguageSyncEnabledFlow>()
    factoryOf(::SetLanguageSyncEnabledUseCase).bind<SetLanguageSyncEnabled>()
    factoryOf(::ObserveLanguageSyncUseCase).bind<ObserveLanguageSync>()

    // Presentation
    factoryOf(::AppLanguageUiStateFactory)
    viewModelOf(::AppLanguageViewModel)
}
