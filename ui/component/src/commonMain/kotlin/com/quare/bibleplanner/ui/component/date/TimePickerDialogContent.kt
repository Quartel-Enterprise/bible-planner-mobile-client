package com.quare.bibleplanner.ui.component.date

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePickerState

private val contentPadding = 24.dp
private val buttonsSpacing = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogContent(
    state: AdaptiveTimePickerState,
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
) {
    Surface(
        shape = TimePickerDialogDefaults.shape,
        color = TimePickerDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            title()
            AdaptiveTimePicker(
                state = state,
                modifier = Modifier.nativeTimePickerSize(),
            )
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(buttonsSpacing),
            ) {
                dismissButton()
                confirmButton()
            }
        }
    }
}
