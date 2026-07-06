package com.quare.bibleplanner.feature.readingplan.presentation.component.hero

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.days_ahead_note
import bibleplanner.feature.reading_plan.generated.resources.hero_caughtup_note
import bibleplanner.feature.reading_plan.generated.resources.hero_day_of_total
import bibleplanner.feature.reading_plan.generated.resources.hero_kicker_ahead
import bibleplanner.feature.reading_plan.generated.resources.hero_kicker_caughtup
import bibleplanner.feature.reading_plan.generated.resources.hero_kicker_new
import bibleplanner.feature.reading_plan.generated.resources.hero_kicker_today
import bibleplanner.feature.reading_plan.generated.resources.hero_new_note
import bibleplanner.feature.reading_plan.generated.resources.hero_next_day
import bibleplanner.feature.reading_plan.generated.resources.hero_primary_continue
import bibleplanner.feature.reading_plan.generated.resources.hero_primary_read_ahead
import bibleplanner.feature.reading_plan.generated.resources.hero_primary_read_now
import bibleplanner.feature.reading_plan.generated.resources.hero_primary_start
import bibleplanner.feature.reading_plan.generated.resources.hero_tomorrow_day
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroContainer
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroKicker
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroMarkButton
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroPassage
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroPrimaryButton
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.toDayClick
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.toReadClick
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.toShortLabel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StandardHero(
    planStatus: PlanStatus,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = planStatus.nextDay ?: return
    val mode = planStatus.mode
    val animatedDayIndex by animateIntAsState(targetValue = next.globalIndex, label = "heroDayIndex")
    val animatedDaysAhead by animateIntAsState(targetValue = planStatus.daysAhead, label = "heroDaysAhead")
    HeroContainer(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroKicker(
                icon = mode.kickerIcon(),
                text = stringResource(mode.kickerResource()),
            )
            if (mode.showsHeaderDate()) {
                next.plannedReadDate?.let { date ->
                    Text(
                        text = date.toShortLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        HeroPassage(text = next.passages.toReadingLabel())
        Text(
            text = when (mode) {
                PlanMode.Ahead -> stringResource(Res.string.hero_next_day, animatedDayIndex)
                PlanMode.CaughtUp -> stringResource(Res.string.hero_tomorrow_day, animatedDayIndex)
                else -> stringResource(Res.string.hero_day_of_total, animatedDayIndex, planStatus.totalDays)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        mode.standardNote(daysAhead = planStatus.daysAhead, animatedDaysAhead = animatedDaysAhead)?.let { note ->
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroPrimaryButton(
                modifier = Modifier.weight(1f),
                text = stringResource(mode.primaryResource()),
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                trailingIcon = true,
                onClick = { onEvent(next.toDayClick()) },
            )
            HeroMarkButton(onClick = { onEvent(next.toReadClick()) })
        }
    }
}

private fun PlanMode.kickerResource(): StringResource = when (this) {
    PlanMode.New -> Res.string.hero_kicker_new
    PlanMode.CaughtUp -> Res.string.hero_kicker_caughtup
    PlanMode.Ahead -> Res.string.hero_kicker_ahead
    else -> Res.string.hero_kicker_today
}

private fun PlanMode.kickerIcon(): ImageVector? = when (this) {
    PlanMode.CaughtUp -> Icons.Default.CheckCircle
    PlanMode.Ahead -> Icons.Default.Bolt
    else -> null
}

private fun PlanMode.showsHeaderDate(): Boolean = this != PlanMode.Ahead && this != PlanMode.CaughtUp

private fun PlanMode.primaryResource(): StringResource = when (this) {
    PlanMode.New -> Res.string.hero_primary_start
    PlanMode.CaughtUp -> Res.string.hero_primary_read_ahead
    PlanMode.Ahead -> Res.string.hero_primary_continue
    else -> Res.string.hero_primary_read_now
}

@Composable
private fun PlanMode.standardNote(
    daysAhead: Int,
    animatedDaysAhead: Int,
): String? = when (this) {
    PlanMode.New -> stringResource(Res.string.hero_new_note)
    PlanMode.CaughtUp -> stringResource(Res.string.hero_caughtup_note)
    PlanMode.Ahead -> pluralStringResource(Res.plurals.days_ahead_note, daysAhead, animatedDaysAhead)
    else -> null
}
