package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

@Composable
internal fun ReadingPlanFabsColumn(
    state: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(
            visible = state.isScrolledDown,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SmallFloatingActionButton(onClick = { onEvent(ReadingPlanUiEvent.OnScrollToTopClick) }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.reading_plan_scroll_to_top),
                )
            }
        }
        if (state is ReadingPlanUiState.Loaded) {
            GoToActiveFab(
                isVisible = !state.isActiveRowVisible,
                planMode = state.planStatus.mode,
                onClick = { onEvent(ReadingPlanUiEvent.OnGoToActiveRowClick) },
            )
        }
    }
}

@Composable
private fun GoToActiveFab(
    isVisible: Boolean,
    planMode: PlanMode,
    onClick: () -> Unit,
) {
    val labelRes = planMode.toFabLabelRes() ?: return
    AnimatedVisibility(visible = isVisible) {
        ExtendedFloatingActionButton(
            text = { Text(text = stringResource(labelRes)) },
            icon = {
                Icon(
                    imageVector = planMode.toFabIcon(),
                    contentDescription = null,
                )
            },
            onClick = onClick,
        )
    }
}

private fun PlanMode.toFabLabelRes(): Int? = when (this) {
    PlanMode.Ahead, PlanMode.CaughtUp -> R.string.reading_plan_fab_next_reading
    PlanMode.Behind -> R.string.reading_plan_fab_resume
    PlanMode.New, PlanMode.OnTrack -> R.string.reading_plan_fab_back_to_today
    PlanMode.Done -> null
}

private fun PlanMode.toFabIcon(): ImageVector = when (this) {
    PlanMode.Behind -> Icons.Default.History
    PlanMode.New, PlanMode.OnTrack -> Icons.Default.Today
    PlanMode.Ahead, PlanMode.CaughtUp, PlanMode.Done -> Icons.AutoMirrored.Filled.ArrowForward
}
