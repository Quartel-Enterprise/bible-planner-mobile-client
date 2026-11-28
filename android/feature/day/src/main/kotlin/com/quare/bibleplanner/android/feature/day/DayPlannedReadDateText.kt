package com.quare.bibleplanner.android.feature.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.date.rememberMediumDateLabel
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.ui.utils.toStringResource
import com.quare.bibleplanner.ui.utils.transition.TransitionScopes
import com.quare.bibleplanner.ui.utils.transition.sharedElementOf
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource as sharedStringResource

private const val SHORT_MONTH_LENGTH = 3
private const val DAY_LABEL_LENGTH = 2

// Why: the day, month and year are separate texts so each one flies to and from the plans tab's date column.
@Composable
internal fun DayPlannedReadDateText(
    plannedReadDate: LocalDate,
    weekNumber: Int,
    dayNumber: Int,
    transitionScopes: TransitionScopes?,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.day_planned_read_date)
    // Why: the split texts read the date in pieces, so screen readers get the locale's full medium date instead.
    val spokenDate = "$label ${rememberMediumDateLabel(plannedReadDate)}"
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = spokenDate },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PlannedReadDatePartText(text = label)
        PlannedReadDatePartText(
            text = plannedReadDate.day.toString().padStart(DAY_LABEL_LENGTH, '0'),
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
