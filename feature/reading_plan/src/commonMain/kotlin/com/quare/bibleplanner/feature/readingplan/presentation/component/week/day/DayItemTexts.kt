package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.chip_overdue
import bibleplanner.feature.reading_plan.generated.resources.day_number
import bibleplanner.feature.reading_plan.generated.resources.today_badge
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.readingplan.presentation.component.toReadingLabel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.DayItemTexts(
    modifier: Modifier = Modifier,
    day: DayModel,
    globalDayIndex: Int,
    weekNumber: Int,
    isHighlighted: Boolean,
    isOverdue: Boolean,
    animatedContentScope: AnimatedContentScope,
) {
    val isRead = day.isRead
    val titleColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isHighlighted -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "dayTitleColor",
    )
    val passageColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            isHighlighted -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "dayPassageColor",
    )
    val textDecoration = if (isRead) TextDecoration.LineThrough else TextDecoration.None
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = SharedTransitionAnimationUtils.buildDayNumberId(weekNumber, day.number),
                    ),
                    animatedVisibilityScope = animatedContentScope,
                ),
                text = stringResource(Res.string.day_number, globalDayIndex),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                textDecoration = textDecoration,
            )
            DayStateChip(
                isToday = day.isToday && !isRead,
                isOverdue = isOverdue,
            )
        }
        Text(
            text = day.passages.toReadingLabel(),
            style = MaterialTheme.typography.bodyMedium,
            color = passageColor,
            textDecoration = textDecoration,
        )
    }
}

@Composable
private fun DayStateChip(
    isToday: Boolean,
    isOverdue: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        isToday -> StateChip(
            modifier = modifier,
            text = stringResource(Res.string.today_badge),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )

        isOverdue -> StateChip(
            modifier = modifier,
            text = stringResource(Res.string.chip_overdue),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
private fun StateChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}
