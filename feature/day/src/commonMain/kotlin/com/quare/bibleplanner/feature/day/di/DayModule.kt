package com.quare.bibleplanner.feature.day.di

import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayReadStatusUseCase
import com.quare.bibleplanner.feature.day.data.datasource.DayLocalDataSource
import com.quare.bibleplanner.feature.day.data.mapper.DayEntityToModelMapper
import com.quare.bibleplanner.feature.day.data.repository.DayRepositoryImpl
import com.quare.bibleplanner.feature.day.domain.EditDaySelectableDates
import com.quare.bibleplanner.feature.day.domain.mapper.LocalDateTimeToDateMapper
import com.quare.bibleplanner.feature.day.domain.usecase.CalculateAllChaptersReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.CalculateChapterReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertTimestampToDatePickerInitialDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertUtcDateToLocalDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.DayUseCases
import com.quare.bibleplanner.feature.day.domain.usecase.GetBooksUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.GetDayDetailsUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ToggleChapterReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.UpdateChapterReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.UpdateDayReadTimestampUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.UpdateDayReadTimestampWithDateAndTimeUseCase
import com.quare.bibleplanner.feature.day.presentation.factory.DayUiStateFlowFactory
import com.quare.bibleplanner.feature.day.presentation.mapper.MonthPresentationMapper
import com.quare.bibleplanner.feature.day.presentation.mapper.ReadDateFormatter
import com.quare.bibleplanner.feature.day.presentation.viewmodel.DayViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dayModule = module {
    // Data
    singleOf(::DayLocalDataSource)
    factoryOf(::DayEntityToModelMapper)
    singleOf(::DayRepositoryImpl).bind<DayRepository>()

    // Domain
    factoryOf(::GetDayDetailsUseCase)
    factoryOf(::GetBooksUseCase)
    factoryOf(::UpdateDayReadStatusUseCase)
    factoryOf(::UpdateChapterReadStatusUseCase)
    factoryOf(::UpdateDayReadTimestampUseCase)
    factoryOf(::CalculateChapterReadStatusUseCase)
    factoryOf(::CalculateAllChaptersReadStatusUseCase)
    factoryOf(::ToggleChapterReadStatusUseCase)
    factoryOf(::ConvertUtcDateToLocalDateUseCase)
    factoryOf(::UpdateDayReadTimestampWithDateAndTimeUseCase)
    factoryOf(::ConvertTimestampToDatePickerInitialDateUseCase)
    factoryOf(::EditDaySelectableDates)
    factoryOf(::LocalDateTimeToDateMapper)

    // Use cases container
    factoryOf(::DayUseCases)

    // Presentation
    factoryOf(::DayUiStateFlowFactory)
    factoryOf(::ReadDateFormatter)
    factoryOf(::MonthPresentationMapper)

    // ViewModel
    viewModelOf(::DayViewModel)
}
