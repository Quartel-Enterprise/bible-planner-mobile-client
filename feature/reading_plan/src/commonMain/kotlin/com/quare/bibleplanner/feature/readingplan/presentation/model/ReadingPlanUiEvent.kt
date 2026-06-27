package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal sealed interface ReadingPlanUiEvent {
    data class OnPlanClick(
        val type: ReadingPlanType,
    ) : ReadingPlanUiEvent

    data class OnWeekExpandClick(
        val weekNumber: Int,
    ) : ReadingPlanUiEvent

    data class OnDayReadClick(
        val dayNumber: Int,
        val weekNumber: Int,
    ) : ReadingPlanUiEvent

    data class OnDayClick(
        val dayNumber: Int,
        val weekNumber: Int,
    ) : ReadingPlanUiEvent

    data object OnOverflowClick : ReadingPlanUiEvent

    data object OnOverflowDismiss : ReadingPlanUiEvent

    data class OnOverflowOptionClick(
        val option: OverflowOption,
    ) : ReadingPlanUiEvent

    data object OnToggleUpcomingExpanded : ReadingPlanUiEvent

    data object OnToggleCompletedExpanded : ReadingPlanUiEvent

    data object OnGoToActiveRowClick : ReadingPlanUiEvent

    data object OnSkipToTodayClick : ReadingPlanUiEvent

    data object OnScrollToTopClick : ReadingPlanUiEvent

    data class OnScrollStateChange(
        val isScrolledDown: Boolean,
    ) : ReadingPlanUiEvent

    data class OnActiveRowVisibilityChange(
        val isActiveRowVisible: Boolean,
    ) : ReadingPlanUiEvent

    data object OnScrollToWeekCompleted : ReadingPlanUiEvent

    data object OnScrollToTopCompleted : ReadingPlanUiEvent

    data object OnFlashCompleted : ReadingPlanUiEvent

    data object OnEditPlanClick : ReadingPlanUiEvent
}
