package com.quare.bibleplanner.feature.read.di

import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import com.quare.bibleplanner.feature.read.presentation.ReadViewModel
import com.quare.bibleplanner.feature.read.presentation.factory.ReadDataPresentationModelFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureReadModule = module {
    factoryOf(::GetReadNavigationSuggestionsModelUseCase)
    factoryOf(::ReadDataPresentationModelFactory)
    viewModelOf(::ReadViewModel)
}
