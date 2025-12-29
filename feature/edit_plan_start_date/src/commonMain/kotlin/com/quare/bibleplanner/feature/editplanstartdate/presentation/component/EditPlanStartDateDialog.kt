package com.quare.bibleplanner.feature.editplanstartdate.presentation.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import bibleplanner.feature.edit_plan_start_date.generated.resources.Res
import bibleplanner.feature.edit_plan_start_date.generated.resources.cancel
import bibleplanner.feature.edit_plan_start_date.generated.resources.ok
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EditPlanStartDateDialog(
    initialTimestamp: Long?,
    onEvent: (EditPlanStartDateUiEvent) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialTimestamp,
    )

    DatePickerDialog(
        onDismissRequest = {
            onEvent(EditPlanStartDateUiEvent.OnDismissDialog)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { utcDateMillis ->
                        onEvent(EditPlanStartDateUiEvent.OnDateSelected(utcDateMillis))
                    }
                },
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEvent(EditPlanStartDateUiEvent.OnDismissDialog)
                },
            ) {
                Text(stringResource(Res.string.cancel))
            }
        },
    ) {
        DatePicker(datePickerState)
    }
}
