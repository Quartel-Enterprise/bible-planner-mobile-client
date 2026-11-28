package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.rememberAlertDialogResolution
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnnotationsRangePickerDialog(
    customRange: AnnotationDateRange?,
    today: LocalDate,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    val todayUtcMillis = today.toUtcMillis()
    val selectableDates = remember(today) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayUtcMillis

            override fun isSelectableYear(year: Int): Boolean = year <= today.year
        }
    }
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = customRange?.start?.toUtcMillis(),
        initialSelectedEndDateMillis = customRange?.end?.toUtcMillis(),
        initialDisplayedMonthMillis = customRange?.start?.toUtcMillis() ?: todayUtcMillis,
        selectableDates = selectableDates,
    )
    val resolution = rememberAlertDialogResolution()
    val onDismiss = { resolution.resolve { onEvent(AnnotationsUiEvent.OnCustomRangeDismiss) } }
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null,
                onClick = {
                    state.selectedStartDateMillis?.let { startUtcMillis ->
                        resolution.resolve {
                            onEvent(
                                AnnotationsUiEvent.OnCustomRangeApply(
                                    startUtcMillis = startUtcMillis,
                                    endUtcMillis = state.selectedEndDateMillis,
                                ),
                            )
                        }
                    }
                },
            ) {
                Text(text = stringResource(R.string.annotations_apply_range))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.annotations_cancel))
            }
        },
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
