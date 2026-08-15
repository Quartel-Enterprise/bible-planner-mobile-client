package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface AddNotesFreeWarningUiEvent : UiEvent {
    data object OnSubscribeToPro : AddNotesFreeWarningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.NOTES_LIMIT_SUBSCRIBE_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnCancel : AddNotesFreeWarningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ADD_NOTES_FREE_WARNING_DISMISSED,
            params = emptyMap(),
        )
    }
}
