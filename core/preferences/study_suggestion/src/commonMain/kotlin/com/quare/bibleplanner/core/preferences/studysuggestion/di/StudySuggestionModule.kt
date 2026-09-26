package com.quare.bibleplanner.core.preferences.studysuggestion.di

import com.quare.bibleplanner.core.preferences.studysuggestion.data.repository.StudySuggestionSettingsRepositoryImpl
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.GetStudySuggestionSyncEnabledFlow
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.ObserveStudySuggestionSync
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionEnabled
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionSyncEnabled
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl.GetStudySuggestionSyncEnabledFlowUseCase
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl.ObserveStudySuggestionSettingsUseCase
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl.ObserveStudySuggestionSyncUseCase
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl.SetStudySuggestionEnabledUseCase
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl.SetStudySuggestionModeUseCase
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl.SetStudySuggestionSyncEnabledUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val studySuggestionModule = module {
    singleOf(::StudySuggestionSettingsRepositoryImpl).bind<StudySuggestionSettingsRepository>()

    factoryOf(::ObserveStudySuggestionSettingsUseCase).bind<ObserveStudySuggestionSettings>()
    factoryOf(::SetStudySuggestionEnabledUseCase).bind<SetStudySuggestionEnabled>()
    factoryOf(::SetStudySuggestionModeUseCase).bind<SetStudySuggestionMode>()
    factoryOf(::GetStudySuggestionSyncEnabledFlowUseCase).bind<GetStudySuggestionSyncEnabledFlow>()
    factoryOf(::SetStudySuggestionSyncEnabledUseCase).bind<SetStudySuggestionSyncEnabled>()
    factoryOf(::ObserveStudySuggestionSyncUseCase).bind<ObserveStudySuggestionSync>()
}
