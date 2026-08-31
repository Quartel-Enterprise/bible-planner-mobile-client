package com.quare.bibleplanner.feature.studysuggestion.di

import com.quare.bibleplanner.feature.studysuggestion.data.repository.StudySuggestionSettingsRepositoryImpl
import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.SetStudySuggestionEnabled
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.SetStudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl.ObserveStudySuggestionSettingsUseCase
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl.SetStudySuggestionEnabledUseCase
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl.SetStudySuggestionModeUseCase
import com.quare.bibleplanner.feature.studysuggestion.presentation.viewmodel.StudySuggestionViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val studySuggestionModule = module {
    singleOf(::StudySuggestionSettingsRepositoryImpl).bind<StudySuggestionSettingsRepository>()

    factoryOf(::ObserveStudySuggestionSettingsUseCase).bind<ObserveStudySuggestionSettings>()
    factoryOf(::SetStudySuggestionEnabledUseCase).bind<SetStudySuggestionEnabled>()
    factoryOf(::SetStudySuggestionModeUseCase).bind<SetStudySuggestionMode>()

    viewModelOf(::StudySuggestionViewModel)
}
