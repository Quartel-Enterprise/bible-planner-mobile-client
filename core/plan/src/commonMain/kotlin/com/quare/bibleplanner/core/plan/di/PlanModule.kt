package com.quare.bibleplanner.core.plan.di

import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.ChaptersRangeMapper
import com.quare.bibleplanner.core.plan.data.mapper.ReadingPlanPreferenceMapper
import com.quare.bibleplanner.core.plan.data.mapper.ReadingPlanPreferenceMapperImpl
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.data.repository.PlanRepositoryImpl
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.DeleteDayNotesUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetDaysWithNotesCountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayNotesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val planModule = module {
    // Data sources
    singleOf(::PlanLocalDataSource)

    // Mappers
    factoryOf(::WeekPlanDtoToModelMapper)
    factoryOf(::ChaptersRangeMapper)
    singleOf(::ReadingPlanPreferenceMapperImpl).bind<ReadingPlanPreferenceMapper>()

    // Repository
    singleOf(::PlanRepositoryImpl).bind<PlanRepository>()

    // Use cases
    factoryOf(::GetPlannedReadDateForDayUseCase)
    factoryOf(::GetPlansByWeekUseCase)
    factoryOf(::SetPlanStartTimeUseCase)
    factoryOf(::UpdateDayNotesUseCase)
    factoryOf(::DeleteDayNotesUseCase)
    factoryOf(::GetDaysWithNotesCountUseCase)
    factoryOf(::GetMaxFreeNotesAmountUseCase)
    factoryOf(::GetPlanStartDateFlowUseCase)
}
