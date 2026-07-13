package com.quare.bibleplanner.feature.deleteprogress.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface DeleteAllProgressUiEvent : UiEvent {
    data object OnConfirmDelete : DeleteAllProgressUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PROGRESS_RESET_CONFIRMED,
        )
    }

    data object OnCancel : DeleteAllProgressUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROGRESS_RESET_CANCELLED,
            params = emptyMap(),
        )
    }
}
