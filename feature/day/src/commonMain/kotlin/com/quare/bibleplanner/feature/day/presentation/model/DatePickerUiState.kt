package com.quare.bibleplanner.feature.day.presentation.model

import androidx.compose.material3.SelectableDates
import kotlinx.datetime.LocalDate

internal data class DatePickerUiState(
    val visiblePicker: PickerType?,
    val selectedDateMillis: Long?,
    val selectedLocalDate: LocalDate?,
    val selectableDates: SelectableDates,
    val initialTimestamp: Long,
    val initialHour: Int,
    val initialMinute: Int,
)
