package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.cancel
import bibleplanner.feature.day.generated.resources.next
import bibleplanner.feature.day.generated.resources.ok
import bibleplanner.feature.day.generated.resources.select_time
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.PickerType
import com.quare.bibleplanner.ui.component.date.DatePickerDialogContent
import com.quare.bibleplanner.ui.component.date.PickerContentCrossfadeComponent
import com.quare.bibleplanner.ui.component.date.PickerDialog
import com.quare.bibleplanner.ui.component.date.TimePickerDialogContent
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
    val dismissButton: @Composable () -> Unit = {
        TextButton(
            onClick = {
                onEvent(DayUiEvent.OnDismissPicker)
            },
        ) {
            Text(stringResource(Res.string.cancel))
        }
    }
    PickerDialog(
        onDismissRequest = {
            onEvent(DayUiEvent.OnDismissPicker)
        },
    ) {
        PickerContentCrossfadeComponent(targetState = type) { pickerType ->
            when (pickerType) {
                PickerType.DATE -> DatePickerDialogContent(
                    state = datePickerState,
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
                    dismissButton = dismissButton,
                )

                PickerType.TIME -> TimePickerDialogContent(
                    state = timePickerState,
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
                    dismissButton = dismissButton,
                )
            }
        }
    }
}
