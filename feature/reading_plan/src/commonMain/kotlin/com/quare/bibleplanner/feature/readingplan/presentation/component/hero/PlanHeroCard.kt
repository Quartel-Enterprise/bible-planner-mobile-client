package com.quare.bibleplanner.feature.readingplan.presentation.component.hero

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent

@Composable
internal fun PlanHeroCard(
    planStatus: PlanStatus,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (planStatus.mode) {
        PlanMode.Done -> DoneHero(
            totalDays = planStatus.totalDays,
            modifier = modifier,
        )

        PlanMode.Behind -> BehindHero(
            planStatus = planStatus,
            onEvent = onEvent,
            modifier = modifier,
        )

        else -> StandardHero(
            planStatus = planStatus,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}
