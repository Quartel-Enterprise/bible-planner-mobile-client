package com.quare.bibleplanner.feature.themeselection.di

import com.quare.bibleplanner.feature.themeselection.data.mapper.ThemePreferenceMapper
import com.quare.bibleplanner.feature.themeselection.data.mapper.ThemePreferenceMapperImpl
import com.quare.bibleplanner.feature.themeselection.data.repository.ThemeSelectionRepositoryImpl
import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetContrastType
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeOption
import com.quare.bibleplanner.feature.themeselection.domain.usecase.impl.GetContrastTypeFlowUseCase
import com.quare.bibleplanner.feature.themeselection.domain.usecase.impl.GetThemeOptionFlowUseCase
import com.quare.bibleplanner.feature.themeselection.domain.usecase.impl.SetContrastTypeUseCase
import com.quare.bibleplanner.feature.themeselection.domain.usecase.impl.SetThemeOptionUseCase
import com.quare.bibleplanner.feature.themeselection.presentation.ThemeSelectionViewModel
import com.quare.bibleplanner.feature.themeselection.presentation.factory.ThemeSelectionUiStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val themeSelectionDomainModule = module {
    // Data
    factoryOf(::ThemeSelectionRepositoryImpl).bind<ThemeSelectionRepository>()
    factoryOf(::ThemePreferenceMapperImpl).bind<ThemePreferenceMapper>()

    // Domain
    factoryOf(::GetThemeOptionFlowUseCase).bind<GetThemeOptionFlow>()
    factoryOf(::SetThemeOptionUseCase).bind<SetThemeOption>()
    factoryOf(::GetContrastTypeFlowUseCase).bind<GetContrastTypeFlow>()
    factoryOf(::SetContrastTypeUseCase).bind<SetContrastType>()

    // Presentation
    factoryOf(::ThemeSelectionUiStateFactory)
    viewModelOf(::ThemeSelectionViewModel)
}
