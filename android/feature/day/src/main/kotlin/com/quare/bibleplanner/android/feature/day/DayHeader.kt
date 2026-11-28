package com.quare.bibleplanner.android.feature.day

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.plan.domain.getGlobalDayIndex
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.utils.transition.TransitionScopes
import com.quare.bibleplanner.ui.utils.transition.sharedElementWithRelayoutOf

private const val READ_STATUS_ANIMATION_MILLIS = 300
private val dayActionButtonMinHeight = 44.dp

@Composable
internal fun DayHeaderTitle(
    state: DayUiState.Loaded,
    transitionScopes: TransitionScopes?,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        val dayIndex = getGlobalDayIndex(
            weekNumber = state.weekNumber,
            dayNumber = state.day.number,
        )
        // Why: the title is split into texts for the transitions, but TalkBack still reads it as one.
        Row(
            modifier = Modifier.semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.day_week_title, state.weekNumber),
                modifier = Modifier.sharedElementWithRelayoutOf(
                    transitionScopes = transitionScopes,
                    key = SharedTransitionAnimationUtils.buildWeekNumberId(state.weekNumber),
                ),
            )
            Text(text = " — ")
            Text(
                text = stringResource(R.string.day_day_title, dayIndex),
                modifier = Modifier.sharedElementWithRelayoutOf(
                    transitionScopes = transitionScopes,
                    key = SharedTransitionAnimationUtils.buildDayNumberId(
                        weekNumber = state.weekNumber,
                        dayNumebr = state.day.number,
                    ),
                ),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        DayProgressIndicator(
            completedCount = state.completedPassagesCount,
            totalCount = state.totalPassagesCount,
        )
    }
}

@Composable
private fun DayProgressIndicator(
    completedCount: Int,
    totalCount: Int,
) {
    val style = MaterialTheme.typography.labelMedium
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedContent(
            targetState = completedCount,
            transitionSpec = { countTransition(isIncreasing = targetState > initialState) },
            label = "completed_count",
        ) { count ->
            Text(
                text = count.toString(),
                style = style,
            )
        }
        Text(
            text = "/$totalCount ${stringResource(R.string.day_passages_completed)}",
            style = style,
        )
    }
}

private fun countTransition(isIncreasing: Boolean): ContentTransform {
    val direction = if (isIncreasing) 1 else -1
    return (slideInVertically { height -> height * direction } + fadeIn()) togetherWith
        (slideOutVertically { height -> -height * direction } + fadeOut())
}

@Composable
internal fun DayHeaderTitleSkeleton(
    pulse: () -> Float,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(horizontalAlignment = horizontalAlignment) {
        DaySkeletonBox(
            pulse = pulse,
            modifier = Modifier
                .width(140.dp)
                .height(22.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        DaySkeletonBox(
            pulse = pulse,
            modifier = Modifier
                .width(90.dp)
                .height(14.dp),
        )
    }
}

@Composable
internal fun DayWideHeader(
    onBackClick: () -> Unit,
    title: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 4.dp,
                end = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DayBackButton(onClick = onBackClick)
        title()
    }
}

@Composable
internal fun DayReadStatusButton(
    isDayRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = dayActionButtonMinHeight),
        shape = RoundedCornerShape(percent = 50),
    ) {
        AnimatedContent(
            targetState = isDayRead,
            transitionSpec = {
                (
                    fadeIn(tween(READ_STATUS_ANIMATION_MILLIS)) +
                        slideInVertically(tween(READ_STATUS_ANIMATION_MILLIS)) { height -> height / 2 }
                ) togetherWith (
                    fadeOut(tween(READ_STATUS_ANIMATION_MILLIS)) +
                        slideOutVertically(tween(READ_STATUS_ANIMATION_MILLIS)) { height -> -height / 2 }
                )
            },
            label = "read_status",
        ) { isRead ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRead) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(if (isRead) R.string.day_mark_as_unread else R.string.day_mark_as_read))
            }
        }
    }
}

@Composable
internal fun DayAskAiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = dayActionButtonMinHeight),
        shape = RoundedCornerShape(percent = 50),
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = stringResource(R.string.day_ask_ai))
    }
}

@Composable
internal fun DayAskAiFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
            )
        },
        text = { Text(text = stringResource(R.string.day_ask_ai)) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )
}
