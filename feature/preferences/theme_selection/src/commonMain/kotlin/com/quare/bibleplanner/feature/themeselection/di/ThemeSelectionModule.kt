package com.quare.bibleplanner.feature.themeselection.di

import com.quare.bibleplanner.feature.themeselection.presentation.ThemeSelectionViewModel
import com.quare.bibleplanner.feature.themeselection.presentation.factory.ThemeSelectionUiStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureThemeSelectionModule = module {
    factoryOf(::ThemeSelectionUiStateFactory)
    viewModelOf(::ThemeSelectionViewModel)
}
