package com.quare.bibleplanner.feature.editplanstartdate.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface EditPlanStartDateUiEvent : UiEvent {
    data object OnDismissDialog : EditPlanStartDateUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.EDIT_PLAN_START_DATE_DISMISSED,
            params = emptyMap(),
        )
    }

    data class OnDateSelected(
        val utcDateMillis: Long,
    ) : EditPlanStartDateUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PLAN_START_DATE_CHANGED,
            params = emptyMap(),
        )
    }
}
