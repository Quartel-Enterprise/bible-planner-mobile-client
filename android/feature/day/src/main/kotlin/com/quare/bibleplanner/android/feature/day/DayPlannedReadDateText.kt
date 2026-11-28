package com.quare.bibleplanner.android.feature.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource as sharedStringResource

private const val SHORT_MONTH_LENGTH = 3

// Why: the day, month and year are separate texts so each one flies to and from the plans tab's date column.
@Composable
internal fun DayPlannedReadDateText(
    plannedReadDate: LocalDate,
    weekNumber: Int,
    dayNumber: Int,
    transitionScopes: DayTransitionScopes?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PlannedReadDatePartText(text = stringResource(R.string.day_planned_read_date))
        PlannedReadDatePartText(
            text = plannedReadDate.day.toString(),
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = SharedTransitionAnimationUtils.buildPlannedDay(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
            ),
        )
        PlannedReadDatePartText(
            text = sharedStringResource(plannedReadDate.month.toStringResource()).take(SHORT_MONTH_LENGTH),
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = SharedTransitionAnimationUtils.buildPlannedMonth(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
            ),
        )
        PlannedReadDatePartText(
            text = plannedReadDate.year.toString(),
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = SharedTransitionAnimationUtils.buildPlannedYear(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
            ),
        )
    }
}

@Composable
private fun PlannedReadDatePartText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )
}
