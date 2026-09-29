package com.quare.bibleplanner.feature.verse.annotations.di

import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.ObserveAnnotationEntries
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.ObserveOtherVersionAnnotationCounts
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.impl.ObserveAnnotationEntriesUseCase
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.impl.ObserveOtherVersionAnnotationCountsUseCase
import com.quare.bibleplanner.feature.verse.annotations.presentation.factory.AnnotationsContentFactory
import com.quare.bibleplanner.feature.verse.annotations.presentation.viewmodel.AnnotationsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val annotationsModule = module {
    factoryOf(::ObserveAnnotationEntriesUseCase).bind<ObserveAnnotationEntries>()
    factoryOf(::ObserveOtherVersionAnnotationCountsUseCase).bind<ObserveOtherVersionAnnotationCounts>()
    factoryOf(::AnnotationsContentFactory)
    viewModelOf(::AnnotationsViewModel)
}
