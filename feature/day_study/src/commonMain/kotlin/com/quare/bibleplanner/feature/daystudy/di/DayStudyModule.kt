package com.quare.bibleplanner.feature.daystudy.di

import com.quare.bibleplanner.core.clear.domain.ClearDayStudyLocalData
import com.quare.bibleplanner.feature.daystudy.data.datasource.DayStudyLocalDataSource
import com.quare.bibleplanner.feature.daystudy.data.datasource.DayStudyRemoteDataSource
import com.quare.bibleplanner.feature.daystudy.data.mapper.DayStudyCacheKeyFactory
import com.quare.bibleplanner.feature.daystudy.data.mapper.DayStudyContentMapper
import com.quare.bibleplanner.feature.daystudy.data.mapper.DayStudyEntityMapper
import com.quare.bibleplanner.feature.daystudy.data.mapper.DayStudyPhaseMapper
import com.quare.bibleplanner.feature.daystudy.data.mapper.DayStudyRequestMapper
import com.quare.bibleplanner.feature.daystudy.data.mapper.DayStudyStatusMapper
import com.quare.bibleplanner.feature.daystudy.data.repository.DayStudyRepositoryImpl
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.feature.daystudy.domain.usecase.ClearDayStudyLocalDataUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyBackgroundGenerationViewModel
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyRouteViewModel
import com.quare.bibleplanner.feature.daystudy.presentation.viewmodel.DayStudyViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
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

    singleOf(::DayStudyRemoteDataSource)
    singleOf(::DayStudyLocalDataSource)
    singleOf(::DayStudyRepositoryImpl).bind<DayStudyRepository>()
    singleOf(::DayStudyGenerationCoordinator)

    factoryOf(::GetDayStudyUseCase)
    factoryOf(::GetDayStudyQuotaUseCase)
    factoryOf(::GetDayPassagesForDayStudyUseCase)
    factoryOf(::HasCachedStudyUseCase)
    factoryOf(::ClearDayStudyLocalDataUseCase).bind<ClearDayStudyLocalData>()

    factoryOf(::DayStudyCardUiModelFactory)
    viewModelOf(::DayStudyViewModel)
    viewModelOf(::DayStudyRouteViewModel)
    viewModelOf(::DayStudyBackgroundGenerationViewModel)
}
