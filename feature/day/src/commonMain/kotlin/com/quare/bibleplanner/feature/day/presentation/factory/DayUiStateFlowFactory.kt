package com.quare.bibleplanner.feature.day.presentation.factory

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.day.domain.EditDaySelectableDates
import com.quare.bibleplanner.feature.day.domain.usecase.CalculateAllChaptersReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertTimestampToDatePickerInitialDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.GetBooksUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.GetDayDetailsUseCase
import com.quare.bibleplanner.feature.day.presentation.mapper.ReadDateFormatter
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class DayUiStateFlowFactory(
    private val getDayDetails: GetDayDetailsUseCase,
    private val getBooks: GetBooksUseCase,
    private val readDateFormatter: ReadDateFormatter,
    private val editDaySelectableDates: EditDaySelectableDates,
    private val convertTimestampToDatePickerInitialDate: ConvertTimestampToDatePickerInitialDateUseCase,
    private val calculateAllChaptersReadStatus: CalculateAllChaptersReadStatusUseCase,
) {
    fun createUiState(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        currentState: DayUiState.Loaded?,
    ): Flow<DayUiState> = combine(
        getDayDetails(weekNumber, dayNumber, readingPlanType),
        getBooks(),
    ) { day, books ->
        if (day != null) {
            val existingDatePickerUiState = currentState?.datePickerUiState

            // Calculate initial timestamp and date components
            val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
            val savedTimestamp = day.readTimestamp ?: currentTimeMillis
            // Convert to UTC midnight for DatePicker (DatePicker expects UTC midnight)
            val initialTimestamp = convertTimestampToDatePickerInitialDate(savedTimestamp)
            val initialDate = Instant
                .fromEpochMilliseconds(savedTimestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())

            val datePickerUiState = if (existingDatePickerUiState?.selectableDates != null) {
                existingDatePickerUiState.copy(
                    initialTimestamp = initialTimestamp,
                    initialHour = initialDate.hour,
                    initialMinute = initialDate.minute,
                )
            } else {
                (
                    existingDatePickerUiState ?: DatePickerUiState(
                        visiblePicker = null,
                        selectedDateMillis = null,
                        selectedLocalDate = null,
                        selectableDates = editDaySelectableDates,
                        initialTimestamp = initialTimestamp,
                        initialHour = initialDate.hour,
                        initialMinute = initialDate.minute,
                    )
                ).copy(
                    selectableDates = editDaySelectableDates,
                    initialTimestamp = initialTimestamp,
                    initialHour = initialDate.hour,
                    initialMinute = initialDate.minute,
                )
            }

            DayUiState.Loaded(
                day = day,
                weekNumber = weekNumber,
                books = books,
                datePickerUiState = datePickerUiState,
                formattedReadDate = day.readTimestamp?.let(readDateFormatter::format),
                chapterReadStatus = calculateAllChaptersReadStatus(
                    passages = day.passages,
                    books = books,
                ),
            )
        } else {
            DayUiState.Loading
        }
    }
}
