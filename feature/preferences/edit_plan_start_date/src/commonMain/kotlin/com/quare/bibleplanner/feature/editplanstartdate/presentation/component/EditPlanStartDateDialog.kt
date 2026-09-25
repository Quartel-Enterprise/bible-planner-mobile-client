package com.quare.bibleplanner.feature.editplanstartdate.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import bibleplanner.feature.preferences.edit_plan_start_date.generated.resources.Res
import bibleplanner.feature.preferences.edit_plan_start_date.generated.resources.cancel
import bibleplanner.feature.preferences.edit_plan_start_date.generated.resources.ok
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiEvent
import com.quare.bibleplanner.ui.component.date.DatePickerDialogContent
import com.quare.bibleplanner.ui.component.date.PickerDialogSceneContent
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EditPlanStartDateDialog(
    initialTimestamp: Long?,
    onEvent: (EditPlanStartDateUiEvent) -> Unit,
) {
    val datePickerState = rememberAdaptiveDatePickerState(
        initialSelectedDateMillis = initialTimestamp,
    )

    PickerDialogSceneContent(
        onDismissRequest = {
            onEvent(EditPlanStartDateUiEvent.OnDismissDialog)
        },
    ) {
        DatePickerDialogContent(
            state = datePickerState,
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
        )
    }
}
