package com.quare.bibleplanner.core.plan.di

import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.ChaptersRangeMapper
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.data.repository.PlanRepositoryImpl
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.DeletePlanStartDateUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
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

    // Repository
    singleOf(::PlanRepositoryImpl).bind<PlanRepository>()

    // Use cases
    factoryOf(::GetPlannedReadDateForDayUseCase)
    factoryOf(::GetPlansByWeekUseCase)
    factoryOf(::DeletePlanStartDateUseCase)
}
