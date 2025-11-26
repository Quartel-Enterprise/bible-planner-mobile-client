package com.quare.bibleplanner.feature.readingplan.di

import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ReadingPlanModule = module {
    viewModelOf(::ReadingPlanViewModel)
    factoryOf(::ReadingPlanStateFactory)
}
