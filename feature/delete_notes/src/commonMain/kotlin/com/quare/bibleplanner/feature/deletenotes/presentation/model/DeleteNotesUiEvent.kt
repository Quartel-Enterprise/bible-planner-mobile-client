package com.quare.bibleplanner.feature.deletenotes.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface DeleteNotesUiEvent : UiEvent {
    data object OnConfirmDelete : DeleteNotesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.NOTE_DELETED,
        )
    }

    data object OnCancel : DeleteNotesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.NOTE_DELETE_CANCELLED,
            params = emptyMap(),
        )
    }
}
