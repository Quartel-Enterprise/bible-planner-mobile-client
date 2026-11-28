package com.quare.bibleplanner.android.feature.day

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.date.rememberMediumDateLabel
import kotlinx.datetime.LocalDate

@Composable
internal fun DayPlannedReadDateText(
    plannedReadDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val formattedDate = rememberMediumDateLabel(plannedReadDate)
    Text(
        text = "${stringResource(R.string.day_planned_read_date)} $formattedDate",
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )
}
