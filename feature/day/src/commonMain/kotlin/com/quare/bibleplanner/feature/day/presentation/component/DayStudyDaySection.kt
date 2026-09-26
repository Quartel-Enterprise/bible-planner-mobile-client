package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.presentation.DayStudySectionSlot
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

@Composable
internal fun DayStudyDaySection(
    slot: DayStudySectionSlot,
    passages: List<PassageModel>,
    dayRoute: DayNavRoute,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    slot.Content(
        passages = passages,
        dayRoute = dayRoute,
        onOpenPaywall = { onEvent(DayUiEvent.OnDayStudySubscribeClick) },
        onOpenLoginWarning = { onEvent(DayUiEvent.OnDayStudyLoginRequired) },
        onShowSnackBar = { message -> onEvent(DayUiEvent.OnDayStudyMessage(message)) },
        onNavigateToStudy = { onEvent(DayUiEvent.OnDayStudyNavigate) },
        modifier = modifier,
    )
}
