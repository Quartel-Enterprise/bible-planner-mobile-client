package com.quare.bibleplanner.core.daystudy.di

import com.quare.bibleplanner.core.clear.domain.ClearDayStudyLocalData
import com.quare.bibleplanner.core.daystudy.data.datasource.DayStudyLocalDataSource
import com.quare.bibleplanner.core.daystudy.data.datasource.DayStudyRemoteDataSource
import com.quare.bibleplanner.core.daystudy.data.datasource.DayStudyRemoteDataSourceImpl
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyCacheKeyFactory
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyContentMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyEntityMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyPhaseMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyRequestMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyStatusMapper
import com.quare.bibleplanner.core.daystudy.data.repository.DayStudyRepositoryImpl
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinatorImpl
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.core.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.core.daystudy.domain.usecase.ClearDayStudyLocalDataUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.PrefetchDayStudyQuota
import com.quare.bibleplanner.core.daystudy.domain.usecase.PrefetchDayStudyQuotaUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dayStudyModule = module {
    factoryOf(::BookIdWireNameMapper)
    factoryOf(::LanguageCodeMapper)
    factoryOf(::DayStudyRequestMapper)
    factoryOf(::DayStudyCacheKeyFactory)
    factoryOf(::DayStudyContentMapper)
    factoryOf(::DayStudyEntityMapper)
    factoryOf(::DayStudyStatusMapper)
    factoryOf(::DayStudyPhaseMapper)

    singleOf(::DayStudyRemoteDataSourceImpl).bind<DayStudyRemoteDataSource>()
    singleOf(::DayStudyLocalDataSource)
    singleOf(::DayStudyRepositoryImpl).bind<DayStudyRepository>()
    singleOf(::DayStudyGenerationCoordinatorImpl).bind<DayStudyGenerationCoordinator>()
    singleOf(::DayStudyQuotaPrefetchStore)

    factoryOf(::GetDayStudyUseCase)
    factoryOf(::GetDayStudyQuotaUseCase)
    factoryOf(::GetDayPassagesForDayStudyUseCase)
    factoryOf(::PrefetchDayStudyQuotaUseCase).bind<PrefetchDayStudyQuota>()
    factoryOf(::HasCachedStudyUseCase)
    factoryOf(::ClearDayStudyLocalDataUseCase).bind<ClearDayStudyLocalData>()
}
