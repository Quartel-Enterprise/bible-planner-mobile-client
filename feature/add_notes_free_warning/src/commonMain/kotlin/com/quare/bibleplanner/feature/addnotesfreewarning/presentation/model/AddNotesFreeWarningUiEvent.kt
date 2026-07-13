package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface AddNotesFreeWarningUiEvent : UiEvent {
    data object OnSubscribeToPro : AddNotesFreeWarningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PAYWALL_VIEWED,
            params = mapOf(AnalyticsParams.SOURCE to SOURCE_NOTES_LIMIT),
        )
    }

    data object OnCancel : AddNotesFreeWarningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ADD_NOTES_FREE_WARNING_DISMISSED,
            params = emptyMap(),
        )
    }

    companion object {
        private const val SOURCE_NOTES_LIMIT = "notes_limit"
    }
}
