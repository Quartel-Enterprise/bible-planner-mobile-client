package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.PlannedReadDateComponent(
    modifier: Modifier = Modifier,
    animatedContentScope: AnimatedContentScope,
    plannedReadDate: LocalDate,
    isRead: Boolean,
    shouldShowYear: Boolean,
    weekNumber: Int,
    dayNumber: Int,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val textDecoration = if (isRead) {
            TextDecoration.LineThrough
        } else {
            TextDecoration.None
        }
        val textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = SharedTransitionAnimationUtils.buildPlannedDay(weekNumber, dayNumber),
                    ),
                    animatedVisibilityScope = animatedContentScope,
                ),
                text = plannedReadDate.day.toString().padStart(2, '0'),
                style = textStyle,
                textDecoration = textDecoration,
            )
            Text(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = SharedTransitionAnimationUtils.buildPlannedMonth(weekNumber, dayNumber),
                    ),
                    animatedVisibilityScope = animatedContentScope,
                ),
                text = stringResource(plannedReadDate.month.toStringResource()).take(3),
                style = textStyle,
                textDecoration = textDecoration,
            )
        }

        if (shouldShowYear) {
            Text(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = SharedTransitionAnimationUtils.buildPlannedYear(weekNumber, dayNumber),
                    ),
                    animatedVisibilityScope = animatedContentScope,
                ),
                text = plannedReadDate.year.toString(),
                style = textStyle,
                textDecoration = textDecoration,
            )
        }
    }
}
