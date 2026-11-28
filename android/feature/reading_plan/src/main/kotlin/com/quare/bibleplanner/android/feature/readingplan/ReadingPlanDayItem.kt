package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import kotlinx.datetime.LocalDate

// Why: baseline profile journeys (tools/baseline-profile) open a day through this tag.
private const val PLAN_DAY_TAG = "plan_day"
private const val FLASH_START_ALPHA = 0.28f
private const val FLASH_DURATION_MILLIS = 1400
private const val COLOR_ANIMATION_MILLIS = 400
private const val READ_ALPHA = 0.38f

@Composable
internal fun ReadingPlanDayItem(
    weekNumber: Int,
    dayPlan: DayPlanPresentationModel,
    isFlashing: Boolean,
    transitionScopes: ReadingPlanTransitionScopes?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val day = dayPlan.day
    val dayNumber = day.number
    val isRead = day.isRead
    val isActive = dayPlan.isActive
    val colorAnimationSpec = tween<Color>(
        durationMillis = COLOR_ANIMATION_MILLIS,
        easing = FastOutSlowInEasing,
    )
    val sideBarColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.secondary
            dayPlan.isNextToRead -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = colorAnimationSpec,
        label = "dayItemSideBarColor",
    )
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val rowBackground by animateColorAsState(
        targetValue = if (isActive) highlightColor else highlightColor.copy(alpha = 0f),
        animationSpec = colorAnimationSpec,
        label = "dayItemBackground",
    )
    val flashColor = MaterialTheme.colorScheme.primary
    val flashAlpha = remember { Animatable(0f) }
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(isFlashing) {
        if (isFlashing) {
            flashAlpha.snapTo(FLASH_START_ALPHA)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = FLASH_DURATION_MILLIS),
            )
            currentOnEvent(ReadingPlanUiEvent.OnFlashCompleted)
        }
    }
    Row(
        modifier = Modifier
            .testTag(PLAN_DAY_TAG)
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(rowBackground)
            .drawWithContent {
                drawContent()
                if (flashAlpha.value > 0f) {
                    drawRect(
                        color = flashColor,
                        alpha = flashAlpha.value,
                    )
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
                .padding(
                    start = 12.dp,
                    end = 8.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedContent(
                targetState = day.plannedReadDate,
                label = "plannedReadDate",
            ) { plannedReadDate ->
                if (plannedReadDate != null) {
                    PlannedReadDateColumn(
                        plannedReadDate = plannedReadDate,
                        dayPlan = dayPlan,
                        weekNumber = weekNumber,
                        transitionScopes = transitionScopes,
                    )
                }
            }
            DayTextColumn(
                modifier = Modifier.weight(1f),
                day = day,
                globalDayIndex = dayPlan.globalDayIndex,
                weekNumber = weekNumber,
                isHighlighted = isActive,
                isOverdue = dayPlan.isOverdue,
                transitionScopes = transitionScopes,
            )
            DayReadToggle(
                isRead = isRead,
                isAccented = dayPlan.isAccented,
                contentDescription = stringResource(R.string.reading_plan_day_read, dayNumber),
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

@Composable
private fun PlannedReadDateColumn(
    plannedReadDate: LocalDate,
    dayPlan: DayPlanPresentationModel,
    weekNumber: Int,
    transitionScopes: ReadingPlanTransitionScopes?,
) {
    val dayNumber = dayPlan.day.number
    val isRead = dayPlan.day.isRead
    val textDecoration = if (isRead) TextDecoration.LineThrough else TextDecoration.None
    val dayColor = resolveDateColor(
        baseColor = MaterialTheme.colorScheme.onSurface,
        isRead = isRead,
        isHighlighted = dayPlan.isActive,
        isOverdue = dayPlan.isOverdue,
    )
    val monthColor = resolveDateColor(
        baseColor = MaterialTheme.colorScheme.onSurfaceVariant,
        isRead = isRead,
        isHighlighted = dayPlan.isActive,
        isOverdue = dayPlan.isOverdue,
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = SharedTransitionAnimationUtils.buildPlannedDay(weekNumber, dayNumber),
            ),
            text = plannedReadDate.day.toDayLabel(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = dayColor,
            textDecoration = textDecoration,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.sharedElementOf(
                transitionScopes = transitionScopes,
                key = SharedTransitionAnimationUtils.buildPlannedMonth(weekNumber, dayNumber),
            ),
            text = plannedReadDate.month.toShortMonthLabel(),
            style = MaterialTheme.typography.labelSmall,
            color = monthColor,
            textDecoration = textDecoration,
            textAlign = TextAlign.Center,
        )
        if (dayPlan.shouldShowYear) {
            Text(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .sharedElementOf(
                        transitionScopes = transitionScopes,
                        key = SharedTransitionAnimationUtils.buildPlannedYear(weekNumber, dayNumber),
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
    isRead -> baseColor.copy(alpha = READ_ALPHA)
    isOverdue -> MaterialTheme.colorScheme.tertiary
    isHighlighted -> MaterialTheme.colorScheme.primary
    else -> baseColor
}

@Composable
private fun DayTextColumn(
    day: DayModel,
    globalDayIndex: Int,
    weekNumber: Int,
    isHighlighted: Boolean,
    isOverdue: Boolean,
    transitionScopes: ReadingPlanTransitionScopes?,
    modifier: Modifier = Modifier,
) {
    val isRead = day.isRead
    val titleColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.onSurface.copy(alpha = READ_ALPHA)
            isHighlighted -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "dayTitleColor",
    )
    val passageColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = READ_ALPHA)
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
                modifier = Modifier.sharedElementOf(
                    transitionScopes = transitionScopes,
                    key = SharedTransitionAnimationUtils.buildDayNumberId(weekNumber, day.number),
                ),
                text = stringResource(R.string.reading_plan_day_number, globalDayIndex),
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
) {
    val (text, containerColor, contentColor) = when {
        isToday -> Triple(
            stringResource(R.string.reading_plan_today_badge),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
        )

        isOverdue -> Triple(
            stringResource(R.string.reading_plan_chip_overdue),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )

        else -> return
    }
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 2.dp,
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DayReadToggle(
    isRead: Boolean,
    isAccented: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (isRead) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "dayReadToggleBackground",
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isRead -> Color.Transparent
            isAccented -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "dayReadToggleBorderColor",
    )
    val borderWidth by animateDpAsState(
        targetValue = when {
            isRead -> 0.dp
            isAccented -> 2.dp
            else -> 1.5.dp
        },
        label = "dayReadToggleBorderWidth",
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .semantics { this.contentDescription = contentDescription }
            .toggleable(
                value = isRead,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            ).background(background)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = isRead,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
