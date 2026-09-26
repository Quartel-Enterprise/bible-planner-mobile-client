package com.quare.bibleplanner.feature.studysuggestion.di

import com.quare.bibleplanner.feature.studysuggestion.presentation.factory.StudySuggestionUiStateFactory
import com.quare.bibleplanner.feature.studysuggestion.presentation.viewmodel.StudySuggestionViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureStudySuggestionModule = module {
    factoryOf(::StudySuggestionUiStateFactory)
    viewModelOf(::StudySuggestionViewModel)
}
