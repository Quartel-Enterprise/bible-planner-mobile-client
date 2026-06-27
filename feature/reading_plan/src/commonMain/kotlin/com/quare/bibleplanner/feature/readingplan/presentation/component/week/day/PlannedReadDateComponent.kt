package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    isHighlighted: Boolean,
    isOverdue: Boolean,
    shouldShowYear: Boolean,
    weekNumber: Int,
    dayNumber: Int,
) {
    val textDecoration = if (isRead) TextDecoration.LineThrough else TextDecoration.None
    val dayColor = resolveDateColor(
        baseColor = MaterialTheme.colorScheme.onSurface,
        isRead = isRead,
        isHighlighted = isHighlighted,
        isOverdue = isOverdue,
    )
    val monthColor = resolveDateColor(
        baseColor = MaterialTheme.colorScheme.onSurfaceVariant,
        isRead = isRead,
        isHighlighted = isHighlighted,
        isOverdue = isOverdue,
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            modifier = Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = SharedTransitionAnimationUtils.buildPlannedDay(weekNumber, dayNumber),
                ),
                animatedVisibilityScope = animatedContentScope,
            ),
            text = plannedReadDate.day.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = dayColor,
            textDecoration = textDecoration,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = SharedTransitionAnimationUtils.buildPlannedMonth(weekNumber, dayNumber),
                ),
                animatedVisibilityScope = animatedContentScope,
            ),
            text = stringResource(plannedReadDate.month.toStringResource()).take(3),
            style = MaterialTheme.typography.labelSmall,
            color = monthColor,
            textDecoration = textDecoration,
            textAlign = TextAlign.Center,
        )
        if (shouldShowYear) {
            Text(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = SharedTransitionAnimationUtils.buildPlannedYear(weekNumber, dayNumber),
                        ),
                        animatedVisibilityScope = animatedContentScope,
                    ),
                text = plannedReadDate.year.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = monthColor,
                textDecoration = textDecoration,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun resolveDateColor(
    baseColor: Color,
    isRead: Boolean,
    isHighlighted: Boolean,
    isOverdue: Boolean,
): Color = when {
    isRead -> baseColor.copy(alpha = 0.38f)
    isOverdue -> MaterialTheme.colorScheme.tertiary
    isHighlighted -> MaterialTheme.colorScheme.primary
    else -> baseColor
}
