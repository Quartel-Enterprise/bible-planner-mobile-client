package com.quare.bibleplanner.core.preferences.themeselection.di

import com.quare.bibleplanner.core.preferences.themeselection.data.mapper.ThemePreferenceMapper
import com.quare.bibleplanner.core.preferences.themeselection.data.mapper.ThemePreferenceMapperImpl
import com.quare.bibleplanner.core.preferences.themeselection.data.repository.ThemeSelectionRepositoryImpl
import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetThemeSyncEnabledFlow
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.ObserveThemeSync
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.SetContrastType
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.SetThemeOption
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.SetThemeSyncEnabled
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl.GetContrastTypeFlowUseCase
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl.GetThemeOptionFlowUseCase
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl.GetThemeSyncEnabledFlowUseCase
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl.ObserveThemeSyncUseCase
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl.SetContrastTypeUseCase
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl.SetThemeOptionUseCase
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl.SetThemeSyncEnabledUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val themeSelectionModule = module {
    // Data
    factoryOf(::ThemeSelectionRepositoryImpl).bind<ThemeSelectionRepository>()
    factoryOf(::ThemePreferenceMapperImpl).bind<ThemePreferenceMapper>()

    // Domain
    factoryOf(::GetThemeOptionFlowUseCase).bind<GetThemeOptionFlow>()
    factoryOf(::SetThemeOptionUseCase).bind<SetThemeOption>()
    factoryOf(::GetContrastTypeFlowUseCase).bind<GetContrastTypeFlow>()
    factoryOf(::SetContrastTypeUseCase).bind<SetContrastType>()
    factoryOf(::GetThemeSyncEnabledFlowUseCase).bind<GetThemeSyncEnabledFlow>()
    factoryOf(::SetThemeSyncEnabledUseCase).bind<SetThemeSyncEnabled>()
    factoryOf(::ObserveThemeSyncUseCase).bind<ObserveThemeSync>()
}
