package com.quare.bibleplanner.feature.readingplan.presentation.component.week

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.WeekProgressSeparatorComponent(
    modifier: Modifier = Modifier,
    weekNumber: Int,
    animatedContentScope: AnimatedContentScope,
) {
    Text(
        modifier = modifier.sharedElement(
            sharedContentState = rememberSharedContentState(
                key = SharedTransitionAnimationUtils.buildWeekSeparatorId(weekNumber),
            ),
            animatedVisibilityScope = animatedContentScope,
        ),
        text = "—",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
    )
}
