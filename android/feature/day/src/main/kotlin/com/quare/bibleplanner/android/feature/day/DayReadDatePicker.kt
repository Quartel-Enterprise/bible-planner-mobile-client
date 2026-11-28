package com.quare.bibleplanner.android.feature.day

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.date.isSystem24HourFormat
import com.quare.bibleplanner.feature.day.domain.EditDaySelectableDates
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.PickerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayReadDatePicker(
    picker: PickerType,
    datePickerUiState: DatePickerUiState,
    onEvent: (DayUiEvent) -> Unit,
) {
    val onDismiss = { onEvent(DayUiEvent.OnDismissPicker) }
    val dismissButton: @Composable () -> Unit = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(R.string.day_cancel))
        }
    }
    when (picker) {
        PickerType.DATE -> {
            val selectableDates = datePickerUiState.selectableDates
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = datePickerUiState.initialTimestamp,
                selectableDates = remember(selectableDates) { selectableDates.toSelectableDates() },
            )
            DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { utcDateMillis ->
                                onEvent(DayUiEvent.OnDateSelected(utcDateMillis))
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.day_next))
                    }
                },
                dismissButton = dismissButton,
            ) {
                DatePicker(state = datePickerState)
            }
        }

        PickerType.TIME -> {
            val timePickerState = rememberTimePickerState(
                initialHour = datePickerUiState.initialHour,
                initialMinute = datePickerUiState.initialMinute,
                is24Hour = isSystem24HourFormat(),
            )
            TimePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(
                                DayUiEvent.OnEditReadDate(
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                ),
                            )
                        },
                    ) {
                        Text(text = stringResource(R.string.day_ok))
                    }
                },
                title = { Text(text = stringResource(R.string.day_select_time)) },
                dismissButton = dismissButton,
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun EditDaySelectableDates.toSelectableDates(): SelectableDates = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = this@toSelectableDates.isSelectableDate(utcTimeMillis)

    override fun isSelectableYear(year: Int): Boolean = this@toSelectableDates.isSelectableYear(year)
}
