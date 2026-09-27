package com.quare.bibleplanner.feature.verse.annotations.di

import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.ObserveAnnotationEntries
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.impl.ObserveAnnotationEntriesUseCase
import com.quare.bibleplanner.feature.verse.annotations.presentation.factory.AnnotationsContentFactory
import com.quare.bibleplanner.feature.verse.annotations.presentation.viewmodel.AnnotationsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val annotationsModule = module {
    factoryOf(::ObserveAnnotationEntriesUseCase).bind<ObserveAnnotationEntries>()
    factoryOf(::AnnotationsContentFactory)
    viewModelOf(::AnnotationsViewModel)
}
