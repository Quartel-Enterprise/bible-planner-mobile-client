package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanDayRef
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent

private val heroShape = RoundedCornerShape(24.dp)
private val heroButtonShape = RoundedCornerShape(16.dp)
private val heroButtonHeight = 48.dp

@Composable
internal fun ReadingPlanHeroCard(
    planStatus: PlanStatus,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (planStatus.mode) {
        PlanMode.Done -> DoneHeroCard(
            totalDays = planStatus.totalDays,
            modifier = modifier,
        )

        PlanMode.Behind -> BehindHeroCard(
            planStatus = planStatus,
            onEvent = onEvent,
            modifier = modifier,
        )

        else -> StandardHeroCard(
            planStatus = planStatus,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@Composable
private fun StandardHeroCard(
    planStatus: PlanStatus,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = planStatus.nextDay ?: return
    val mode = planStatus.mode
    val animatedDayIndex by animateIntAsState(
        targetValue = next.globalIndex,
        label = "heroDayIndex",
    )
    val animatedDaysAhead by animateIntAsState(
        targetValue = planStatus.daysAhead,
        label = "heroDaysAhead",
    )
    HeroSurface(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroKickerText(
                icon = mode.toKickerIcon(),
                text = stringResource(mode.toKickerRes()),
            )
            if (mode.showsHeaderDate()) {
                next.plannedReadDate?.let { date ->
                    Text(
                        text = date.toShortDateLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        HeroPassageText(
            modifier = Modifier.padding(top = 4.dp),
            text = next.passages.toReadingLabel(),
        )
        Text(
            text = when (mode) {
                PlanMode.Ahead -> stringResource(R.string.reading_plan_hero_next_day, animatedDayIndex)
                PlanMode.CaughtUp -> stringResource(R.string.reading_plan_hero_tomorrow_day, animatedDayIndex)
                else -> stringResource(R.string.reading_plan_hero_day_of_total, animatedDayIndex, planStatus.totalDays)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        mode
            .standardNote(
                daysAhead = planStatus.daysAhead,
                animatedDaysAhead = animatedDaysAhead,
            )?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroPrimaryButton(
                modifier = Modifier.weight(1f),
                text = stringResource(mode.toPrimaryRes()),
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = { onEvent(next.toDayClick()) },
            )
            HeroMarkButton(onClick = { onEvent(next.toReadClick()) })
        }
    }
}

@Composable
private fun BehindHeroCard(
    planStatus: PlanStatus,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = planStatus.nextDay ?: return
    val animatedDayIndex by animateIntAsState(
        targetValue = next.globalIndex,
        label = "behindHeroDayIndex",
    )
    val animatedDaysBehind by animateIntAsState(
        targetValue = planStatus.daysBehind,
        label = "behindHeroDaysBehind",
    )
    HeroSurface(modifier = modifier) {
        HeroKickerText(
            icon = Icons.Default.History,
            text = stringResource(R.string.reading_plan_hero_kicker_behind),
        )
        HeroPassageText(
            modifier = Modifier.padding(top = 4.dp),
            text = next.passages.toReadingLabel(),
        )
        Text(
            text = stringResource(
                R.string.reading_plan_hero_behind_day,
                animatedDayIndex,
                next.plannedReadDate?.toShortDateLabel().orEmpty(),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = pluralStringResource(
                R.plurals.reading_plan_days_behind_reassure,
                planStatus.daysBehind,
                animatedDaysBehind,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        HeroPrimaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            text = stringResource(R.string.reading_plan_hero_primary_resume),
            icon = Icons.Default.PlayArrow,
            onClick = { onEvent(next.toResumeReadingClick()) },
        )
        planStatus.todayDay?.let { today ->
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(ReadingPlanUiEvent.OnSkipToTodayClick) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Text(
                    text = stringResource(
                        R.string.reading_plan_hero_skip_to_today,
                        today.globalIndex,
                        today.passages.toReadingLabel(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DoneHeroCard(
    totalDays: Int,
    modifier: Modifier = Modifier,
) {
    HeroSurface(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            imageVector = Icons.Default.MilitaryTech,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = stringResource(R.string.reading_plan_done_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = pluralStringResource(R.plurals.reading_plan_done_subtitle, totalDays, totalDays),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun HeroSurface(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = heroShape,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = horizontalAlignment,
        ) {
            content()
        }
    }
}

@Composable
private fun HeroKickerText(
    icon: ImageVector?,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(17.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun HeroPassageText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Composable
private fun HeroPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier.height(heroButtonHeight),
        onClick = onClick,
        shape = heroButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
        Icon(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(20.dp),
            imageVector = icon,
            contentDescription = null,
        )
    }
}

@Composable
private fun HeroMarkButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(heroButtonHeight)
            .clip(heroButtonShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = Icons.Default.Check,
            contentDescription = stringResource(R.string.reading_plan_mark_as_read),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

private fun PlanDayRef.toDayClick(): ReadingPlanUiEvent = ReadingPlanUiEvent.OnDayClick(
    dayNumber = dayNumber,
    weekNumber = weekNumber,
)

private fun PlanDayRef.toReadClick(): ReadingPlanUiEvent = ReadingPlanUiEvent.OnDayReadClick(
    dayNumber = dayNumber,
    weekNumber = weekNumber,
)

private fun PlanDayRef.toResumeReadingClick(): ReadingPlanUiEvent = ReadingPlanUiEvent.OnResumeReadingClick(
    dayNumber = dayNumber,
    weekNumber = weekNumber,
)

private fun PlanMode.toKickerRes(): Int = when (this) {
    PlanMode.New -> R.string.reading_plan_hero_kicker_new
    PlanMode.CaughtUp -> R.string.reading_plan_hero_kicker_caughtup
    PlanMode.Ahead -> R.string.reading_plan_hero_kicker_ahead
    else -> R.string.reading_plan_hero_kicker_today
}

private fun PlanMode.toKickerIcon(): ImageVector? = when (this) {
    PlanMode.CaughtUp -> Icons.Default.CheckCircle
    PlanMode.Ahead -> Icons.Default.Bolt
    else -> null
}

private fun PlanMode.showsHeaderDate(): Boolean = this != PlanMode.Ahead && this != PlanMode.CaughtUp

private fun PlanMode.toPrimaryRes(): Int = when (this) {
    PlanMode.New -> R.string.reading_plan_hero_primary_start
    PlanMode.CaughtUp -> R.string.reading_plan_hero_primary_read_ahead
    PlanMode.Ahead -> R.string.reading_plan_hero_primary_continue
    else -> R.string.reading_plan_hero_primary_read_now
}

@Composable
private fun PlanMode.standardNote(
    daysAhead: Int,
    animatedDaysAhead: Int,
): String? = when (this) {
    PlanMode.New -> stringResource(R.string.reading_plan_hero_new_note)
    PlanMode.CaughtUp -> stringResource(R.string.reading_plan_hero_caughtup_note)
    PlanMode.Ahead -> pluralStringResource(R.plurals.reading_plan_days_ahead_note, daysAhead, animatedDaysAhead)
    else -> null
}
