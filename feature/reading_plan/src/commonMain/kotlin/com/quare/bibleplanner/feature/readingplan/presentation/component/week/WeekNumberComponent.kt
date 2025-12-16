package com.quare.bibleplanner.feature.readingplan.presentation.component.week

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.week_number
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.WeekNumberComponent(
    modifier: Modifier = Modifier,
    weekNumber: Int,
    animatedContentScope: AnimatedContentScope,
) {
    Text(
        modifier = modifier.sharedElement(
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
}
