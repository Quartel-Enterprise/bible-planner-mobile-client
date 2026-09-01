package com.quare.bibleplanner.feature.dayreadingcomplete.di

import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ClassifyDayTimingUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ResolveStudyCtaStateUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel.DayReadingCompleteBannerViewModel
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel.DayReadingCompleteViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dayReadingCompleteModule = module {
    factoryOf(::ClassifyDayTimingUseCase)
    factoryOf(::ResolveStudyCtaStateUseCase)
    viewModelOf(::DayReadingCompleteViewModel)
    viewModelOf(::DayReadingCompleteBannerViewModel)
}
