package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.apply_range
import bibleplanner.feature.verse.annotations.generated.resources.cancel
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDateRangePickerState
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.ui.component.date.DateRangePickerDialogContent
import com.quare.bibleplanner.ui.component.date.PickerDialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomRangePickerDialog(
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
    val state = rememberAdaptiveDateRangePickerState(
        initialSelectedStartDateMillis = customRange?.start?.toUtcMillis(),
        initialSelectedEndDateMillis = customRange?.end?.toUtcMillis(),
        initialDisplayedMonthMillis = customRange?.start?.toUtcMillis() ?: todayUtcMillis,
        selectableDates = selectableDates,
    )
    PickerDialog(onDismissRequest = { onEvent(AnnotationsUiEvent.OnCustomRangeDismiss) }) {
        DateRangePickerDialogContent(
            state = state,
            confirmButton = {
                TextButton(
                    enabled = state.selectedStartDateMillis != null,
                    onClick = {
                        state.selectedStartDateMillis?.let { startUtcMillis ->
                            onEvent(
                                AnnotationsUiEvent.OnCustomRangeApply(
                                    startUtcMillis = startUtcMillis,
                                    endUtcMillis = state.selectedEndDateMillis,
                                ),
                            )
                        }
                    },
                ) {
                    Text(text = stringResource(Res.string.apply_range))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AnnotationsUiEvent.OnCustomRangeDismiss) }) {
                    Text(text = stringResource(Res.string.cancel))
                }
            },
        )
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
