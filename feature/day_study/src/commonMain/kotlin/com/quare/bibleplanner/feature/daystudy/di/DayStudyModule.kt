package com.quare.bibleplanner.feature.daystudy.di

import com.quare.bibleplanner.feature.daystudy.data.repository.DayStudyPanelRatioRepositoryImpl
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyPanelRatioRepository
import com.quare.bibleplanner.feature.daystudy.domain.usecase.ObserveDayStudyPanelReadingFractionUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.SetDayStudyPanelReadingFractionUseCase
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyBackgroundGenerationViewModel
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyPanelViewModel
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyRouteViewModel
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureDayStudyModule = module {
    singleOf(::DayStudyPanelRatioRepositoryImpl).bind<DayStudyPanelRatioRepository>()
    factoryOf(::ObserveDayStudyPanelReadingFractionUseCase)
    factoryOf(::SetDayStudyPanelReadingFractionUseCase)

    factoryOf(::DayStudyCardUiModelFactory)
    viewModelOf(::DayStudyViewModel)
    viewModelOf(::DayStudyRouteViewModel)
    viewModelOf(::DayStudyBackgroundGenerationViewModel)
    viewModelOf(::DayStudyPanelViewModel)
}
