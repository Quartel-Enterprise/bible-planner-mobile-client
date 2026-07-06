package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.presentation.component.DayStudySectionForDay
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

@Composable
internal fun LoadedDayLandscapeScreenRightContent(
    day: DayModel,
    dayRoute: DayNavRoute,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    DayStudySectionForDay(
        passages = day.passages,
        dayRoute = dayRoute,
        onEvent = onEvent,
        modifier = modifier,
        inline = true,
    )
}
