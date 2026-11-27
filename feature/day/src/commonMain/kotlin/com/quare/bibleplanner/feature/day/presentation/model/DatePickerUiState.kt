package com.quare.bibleplanner.feature.day.presentation.model

import com.quare.bibleplanner.feature.day.domain.EditDaySelectableDates
import kotlinx.datetime.LocalDate

internal data class DatePickerUiState(
    val visiblePicker: PickerType?,
    val selectedDateMillis: Long?,
    val selectedLocalDate: LocalDate?,
    val selectableDates: EditDaySelectableDates,
    val initialTimestamp: Long,
    val initialHour: Int,
    val initialMinute: Int,
)
