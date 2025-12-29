package com.quare.bibleplanner.feature.day.presentation.content

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
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.planned_read_date
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.day.presentation.mapper.MonthPresentationMapper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val monthPresentationMapper = MonthPresentationMapper()

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.PlannedReadDateComponent(
    modifier: Modifier = Modifier,
    animatedContentScope: AnimatedContentScope,
    plannedReadDate: LocalDate,
    weekNumber: Int,
    dayNumber: Int,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CommonPlannedReadDateText(
            text = stringResource(
                Res.string.planned_read_date,
            ),
        )
        CommonPlannedReadDateText(
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = SharedTransitionAnimationUtils.buildPlannedDay(weekNumber, dayNumber),
                ),
                animatedVisibilityScope = animatedContentScope,
            ),
            text = plannedReadDate.day.toString(),
        )
        CommonPlannedReadDateText(
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = SharedTransitionAnimationUtils.buildPlannedMonth(weekNumber, dayNumber),
                ),
                animatedVisibilityScope = animatedContentScope,
            ),
            text = stringResource(plannedReadDate.month.toMonthResource()).take(3),
        )
        CommonPlannedReadDateText(
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = SharedTransitionAnimationUtils.buildPlannedYear(weekNumber, dayNumber),
                ),
                animatedVisibilityScope = animatedContentScope,
            ),
            text = plannedReadDate.year.toString(),
        )
    }
}

@Composable
private fun CommonPlannedReadDateText(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun Month.toMonthResource(): StringResource = monthPresentationMapper.map(this)
