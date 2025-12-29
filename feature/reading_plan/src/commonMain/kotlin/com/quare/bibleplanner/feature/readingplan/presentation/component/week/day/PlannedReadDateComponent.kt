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
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.april
import bibleplanner.feature.reading_plan.generated.resources.august
import bibleplanner.feature.reading_plan.generated.resources.december
import bibleplanner.feature.reading_plan.generated.resources.february
import bibleplanner.feature.reading_plan.generated.resources.january
import bibleplanner.feature.reading_plan.generated.resources.july
import bibleplanner.feature.reading_plan.generated.resources.june
import bibleplanner.feature.reading_plan.generated.resources.march
import bibleplanner.feature.reading_plan.generated.resources.may
import bibleplanner.feature.reading_plan.generated.resources.november
import bibleplanner.feature.reading_plan.generated.resources.october
import bibleplanner.feature.reading_plan.generated.resources.september
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PlannedReadDateComponent(
    modifier: Modifier = Modifier,
    animatedContentScope: AnimatedContentScope,
    plannedReadDate: LocalDate,
    isRead: Boolean,
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

private fun Month.toStringResource(): StringResource = when (this) {
    Month.JANUARY -> Res.string.january
    Month.FEBRUARY -> Res.string.february
    Month.MARCH -> Res.string.march
    Month.APRIL -> Res.string.april
    Month.MAY -> Res.string.may
    Month.JUNE -> Res.string.june
    Month.JULY -> Res.string.july
    Month.AUGUST -> Res.string.august
    Month.SEPTEMBER -> Res.string.september
    Month.OCTOBER -> Res.string.october
    Month.NOVEMBER -> Res.string.november
    Month.DECEMBER -> Res.string.december
}
