package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.week_number
import bibleplanner.feature.reading_plan.generated.resources.week_progress_description
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.WeekText(
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    readDaysCount: Int,
    totalDays: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            modifier = Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = SharedTransitionAnimationUtils.buildWeekNumberId(weekNumber),
                ),
                animatedVisibilityScope = animatedContentScope,
            ),
            text = stringResource(
                Res.string.week_number,
                weekNumber,
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            modifier = Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = SharedTransitionAnimationUtils.buildWeekSeparatorId(weekNumber),
                ),
                animatedVisibilityScope = animatedContentScope,
            ),
            text = "—",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(
                Res.string.week_progress_description,
                readDaysCount,
                totalDays,
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
