package com.quare.bibleplanner.android.feature.editplanstartdate

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.rememberAlertDialogResolution
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiEvent

@Composable
internal fun EditPlanStartDateContent(
    initialTimestamp: Long,
    onEvent: (EditPlanStartDateUiEvent) -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialTimestamp)
    // Why: a second tap would save the date and navigate back twice, closing the screen below.
    val resolution = rememberAlertDialogResolution()
    val dismiss = { resolution.resolve { onEvent(EditPlanStartDateUiEvent.OnDismissDialog) } }
    val selectedDateMillis = datePickerState.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = dismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    selectedDateMillis?.let { utcDateMillis ->
                        resolution.resolve { onEvent(EditPlanStartDateUiEvent.OnDateSelected(utcDateMillis)) }
                    }
                },
                enabled = selectedDateMillis != null,
            ) {
                Text(text = stringResource(R.string.edit_plan_start_date_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text(text = stringResource(R.string.edit_plan_start_date_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
