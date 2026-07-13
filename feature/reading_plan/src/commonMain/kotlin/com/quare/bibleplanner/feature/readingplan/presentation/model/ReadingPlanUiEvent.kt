package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface ReadingPlanUiEvent : UiEvent {
    data class OnPlanClick(
        val type: ReadingPlanType,
    ) : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PLAN_SELECTED,
        )
    }

    data class OnWeekExpandClick(
        val weekNumber: Int,
    ) : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PLAN_WEEK_TOGGLED,
        )
    }

    data class OnDayReadClick(
        val dayNumber: Int,
        val weekNumber: Int,
    ) : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DAY_READ_TOGGLED,
        )
    }

    data class OnDayClick(
        val dayNumber: Int,
        val weekNumber: Int,
    ) : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_CARD_CLICKED,
            params = mapOf(
                AnalyticsParams.WEEK_NUMBER to weekNumber,
                AnalyticsParams.DAY_NUMBER to dayNumber,
            ),
        )
    }

    data object OnOverflowClick : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_OVERFLOW_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnOverflowDismiss : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_OVERFLOW_DISMISSED,
            params = emptyMap(),
        )
    }

    data class OnOverflowOptionClick(
        val option: OverflowOption,
    ) : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_OVERFLOW_OPTION_CLICKED,
            params = mapOf(AnalyticsParams.OPTION to option.name.lowercase()),
        )
    }

    data object OnToggleUpcomingExpanded : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PLAN_GROUP_TOGGLED,
        )
    }

    data object OnToggleCompletedExpanded : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PLAN_GROUP_TOGGLED,
        )
    }

    data object OnGoToActiveRowClick : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_SHORTCUT_USED,
            params = mapOf(AnalyticsParams.SHORTCUT to SHORTCUT_ACTIVE_ROW),
        )
    }

    data object OnSkipToTodayClick : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_SHORTCUT_USED,
            params = mapOf(AnalyticsParams.SHORTCUT to SHORTCUT_TODAY),
        )
    }

    data object OnScrollToTopClick : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_SHORTCUT_USED,
            params = mapOf(AnalyticsParams.SHORTCUT to SHORTCUT_SCROLL_TOP),
        )
    }

    data class OnScrollStateChange(
        val isScrolledDown: Boolean,
    ) : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnActiveRowVisibilityChange(
        val isActiveRowVisible: Boolean,
    ) : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnScrollToWeekCompleted : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnScrollToTopCompleted : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnFlashCompleted : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnEditPlanClick : ReadingPlanUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_EDIT_CLICKED,
            params = emptyMap(),
        )
    }

    private companion object {
        const val SHORTCUT_ACTIVE_ROW = "active_row"
        const val SHORTCUT_TODAY = "today"
        const val SHORTCUT_SCROLL_TOP = "scroll_top"
    }
}
