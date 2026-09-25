package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerDialog
import androidx.compose.runtime.Composable
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.cancel
import bibleplanner.feature.day.generated.resources.next
import bibleplanner.feature.day.generated.resources.ok
import bibleplanner.feature.day.generated.resources.select_time
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.PickerType
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TimeEditionDialog(
    type: PickerType,
    onEvent: (DayUiEvent) -> Unit,
    datePickerUiState: DatePickerUiState,
) {
    val datePickerState = rememberAdaptiveDatePickerState(
        initialSelectedDateMillis = datePickerUiState.initialTimestamp,
        selectableDates = datePickerUiState.selectableDates,
    )
    val timePickerState = rememberAdaptiveTimePickerState(
        initialHour = datePickerUiState.initialHour,
        initialMinute = datePickerUiState.initialMinute,
        is24Hour = true,
    )
    when (type) {
        PickerType.DATE -> DatePickerDialog(
            onDismissRequest = {
                onEvent(DayUiEvent.OnDismissPicker)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcDateMillis ->
                            onEvent(DayUiEvent.OnDateSelected(utcDateMillis))
                        }
                    },
                ) {
                    Text(stringResource(Res.string.next))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(DayUiEvent.OnDismissPicker)
                    },
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        ) {
            AdaptiveDatePicker(datePickerState)
        }

        PickerType.TIME -> TimePickerDialog(
            onDismissRequest = {
                onEvent(DayUiEvent.OnDismissPicker)
            },
            title = {
                Text(stringResource(Res.string.select_time))
            },
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
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(DayUiEvent.OnDismissPicker)
                    },
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        ) {
            AdaptiveTimePicker(timePickerState)
        }
    }
}
