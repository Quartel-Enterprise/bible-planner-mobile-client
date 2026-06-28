package com.quare.bibleplanner.feature.readingplan.presentation.component.hero

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.days_behind_reassure
import bibleplanner.feature.reading_plan.generated.resources.hero_behind_day
import bibleplanner.feature.reading_plan.generated.resources.hero_kicker_behind
import bibleplanner.feature.reading_plan.generated.resources.hero_primary_resume
import bibleplanner.feature.reading_plan.generated.resources.hero_skip_to_today
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroContainer
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroKicker
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroPassage
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.HeroPrimaryButton
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.toDayClick
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component.toShortLabel
import com.quare.bibleplanner.feature.readingplan.presentation.component.toReadingLabel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BehindHero(
    planStatus: PlanStatus,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = planStatus.nextDay ?: return
    val animatedDayIndex by animateIntAsState(targetValue = next.globalIndex, label = "behindHeroDayIndex")
    val animatedDaysBehind by animateIntAsState(targetValue = planStatus.daysBehind, label = "behindHeroDaysBehind")
    HeroContainer(modifier = modifier) {
        HeroKicker(
            icon = Icons.Default.History,
            text = stringResource(Res.string.hero_kicker_behind),
        )
        HeroPassage(text = next.passages.toReadingLabel())
        Text(
            text = stringResource(
                Res.string.hero_behind_day,
                animatedDayIndex,
                next.plannedReadDate?.toShortLabel().orEmpty(),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = pluralStringResource(
                Res.plurals.days_behind_reassure,
                planStatus.daysBehind,
                animatedDaysBehind,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        HeroPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.hero_primary_resume),
            icon = Icons.Default.PlayArrow,
            trailingIcon = true,
            onClick = { onEvent(next.toDayClick()) },
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
                        Res.string.hero_skip_to_today,
                        today.globalIndex,
                        today.passages.toReadingLabel(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
