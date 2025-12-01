package com.quare.bibleplanner.feature.readingplan.di

import com.quare.bibleplanner.feature.readingplan.data.mapper.ReadingPlanPreferenceMapper
import com.quare.bibleplanner.feature.readingplan.data.mapper.ReadingPlanPreferenceMapperImpl
import com.quare.bibleplanner.feature.readingplan.data.repository.ReadingPlanRepositoryImpl
import com.quare.bibleplanner.feature.readingplan.domain.repository.ReadingPlanRepository
import com.quare.bibleplanner.feature.readingplan.domain.usecase.FindFirstWeekWithUnreadBook
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetSelectedReadingPlanFlow
import com.quare.bibleplanner.feature.readingplan.domain.usecase.SetSelectedReadingPlan
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.FindFirstWeekWithUnreadBookUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.GetSelectedReadingPlanFlowUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.SetSelectedReadingPlanUseCase
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val readingPlanModule = module {
    // Data
    factoryOf(::ReadingPlanRepositoryImpl).bind<ReadingPlanRepository>()
    factoryOf(::ReadingPlanPreferenceMapperImpl).bind<ReadingPlanPreferenceMapper>()

    // Domain
    factoryOf(::GetSelectedReadingPlanFlowUseCase).bind<GetSelectedReadingPlanFlow>()
    factoryOf(::SetSelectedReadingPlanUseCase).bind<SetSelectedReadingPlan>()
    factoryOf(::FindFirstWeekWithUnreadBookUseCase).bind<FindFirstWeekWithUnreadBook>()

    // Presentation
    viewModelOf(::ReadingPlanViewModel)
    factoryOf(::ReadingPlanStateFactory)
}
