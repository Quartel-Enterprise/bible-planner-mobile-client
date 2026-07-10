package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent

private const val FLASH_START_ALPHA = 0.28f
private const val FLASH_DURATION_MILLIS = 1400

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.DayItem(
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    dayPlan: DayPlanPresentationModel,
    isFlashing: Boolean,
    modifier: Modifier = Modifier,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val day = dayPlan.day
    val dayNumber = day.number
    val isRead = day.isRead
    val isNextToRead = dayPlan.isNextToRead
    val isActive = dayPlan.isActive

    val colorAnimationSpec = tween<Color>(
        durationMillis = 400,
        easing = FastOutSlowInEasing,
    )
    val sideBarColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.secondary
            isNextToRead -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = colorAnimationSpec,
        label = "dayItemSideBarColor",
    )
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val rowBackground by animateColorAsState(
        targetValue = if (isActive) {
            highlightColor
        } else {
            highlightColor.copy(alpha = 0f)
        },
        animationSpec = colorAnimationSpec,
        label = "dayItemBackground",
    )

    val flashColor = MaterialTheme.colorScheme.primary
    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(isFlashing) {
        if (isFlashing) {
            flashAlpha.snapTo(FLASH_START_ALPHA)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = FLASH_DURATION_MILLIS),
            )
            onEvent(ReadingPlanUiEvent.OnFlashCompleted)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(rowBackground)
            .drawWithContent {
                drawContent()
                if (flashAlpha.value > 0f) {
                    drawRect(color = flashColor, alpha = flashAlpha.value)
                }
            }.clickable {
                onEvent(
                    ReadingPlanUiEvent.OnDayClick(
                        dayNumber = dayNumber,
                        weekNumber = weekNumber,
                    ),
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(sideBarColor),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedPlannedReadDateComponent(
                dayPlan = dayPlan,
                animatedContentScope = animatedContentScope,
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                isHighlighted = isActive,
            )
            Box(modifier = Modifier.weight(1f)) {
                DayItemTexts(
                    animatedContentScope = animatedContentScope,
                    day = day,
                    globalDayIndex = dayPlan.globalDayIndex,
                    weekNumber = weekNumber,
                    isHighlighted = isActive,
                    isOverdue = dayPlan.isOverdue,
                )
            }
            DayReadToggle(
                isRead = isRead,
                isAccented = dayPlan.isAccented,
                onClick = {
                    onEvent(
                        ReadingPlanUiEvent.OnDayReadClick(
                            dayNumber = dayNumber,
                            weekNumber = weekNumber,
                        ),
                    )
                },
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
