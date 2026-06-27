package com.quare.bibleplanner.feature.readingplan.di

import com.quare.bibleplanner.feature.readingplan.domain.usecase.FindFirstWeekWithUnreadBook
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetPlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetSelectedReadingPlanFlow
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveDaySituationMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveMilestoneMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveOverallProgressMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolvePlanStatus
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveStreakMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.SetSelectedReadingPlan
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.FindFirstWeekWithUnreadBookUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.GetPlanMotivationMessageUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.GetSelectedReadingPlanFlowUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.ResolveDaySituationMotivationUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.ResolveMilestoneMotivationUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.ResolveOverallProgressMotivationUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.ResolvePlanStatusUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.ResolveStreakMotivationUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.SetSelectedReadingPlanUseCase
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.DeleteProgressMapper
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.WeeksPlanPresentationMapper
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val readingPlanModule = module {

    // Domain
    factoryOf(::GetSelectedReadingPlanFlowUseCase).bind<GetSelectedReadingPlanFlow>()
    factoryOf(::SetSelectedReadingPlanUseCase).bind<SetSelectedReadingPlan>()
    factoryOf(::FindFirstWeekWithUnreadBookUseCase).bind<FindFirstWeekWithUnreadBook>()
    factoryOf(::ResolveMilestoneMotivationUseCase).bind<ResolveMilestoneMotivation>()
    factoryOf(::ResolveStreakMotivationUseCase).bind<ResolveStreakMotivation>()
    factoryOf(::ResolveDaySituationMotivationUseCase).bind<ResolveDaySituationMotivation>()
    factoryOf(::ResolveOverallProgressMotivationUseCase).bind<ResolveOverallProgressMotivation>()
    factoryOf(::GetPlanMotivationMessageUseCase).bind<GetPlanMotivationMessage>()
    factoryOf(::ResolvePlanStatusUseCase).bind<ResolvePlanStatus>()

    // Presentation
    viewModelOf(::ReadingPlanViewModel)
    factoryOf(::ReadingPlanStateFactory)
    factoryOf(::WeeksPlanPresentationMapper)
    factoryOf(::DeleteProgressMapper)
}
