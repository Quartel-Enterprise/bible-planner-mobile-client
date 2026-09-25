package com.quare.bibleplanner.ui.component.date

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePickerState

private val containerWidth = 360.dp
private val containerMaxHeight = 568.dp
private val buttonsPadding = PaddingValues(
    bottom = 8.dp,
    end = 6.dp,
)
private val buttonsSpacing = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogContent(
    state: AdaptiveDatePickerState,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .requiredWidth(containerWidth)
            .heightIn(max = containerMaxHeight),
        shape = DatePickerDefaults.shape,
        color = DatePickerDefaults.colors().containerColor,
        tonalElevation = DatePickerDefaults.TonalElevation,
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.weight(weight = 1f, fill = false)) {
                AdaptiveDatePicker(state)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(buttonsPadding),
                horizontalArrangement = Arrangement.spacedBy(buttonsSpacing),
            ) {
                dismissButton()
                confirmButton()
            }
        }
    }
}
