package com.quare.bibleplanner.feature.read.presentation.deletecolor

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface DeleteHighlightColorUiEvent : UiEvent {
    /** Removing the colour from the palette does not have to remove what was marked with it. */
    data class OnConfirmClick(
        val shouldKeepHighlights: Boolean,
    ) : DeleteHighlightColorUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_DELETED,
        )
    }

    data object OnCancelClick : DeleteHighlightColorUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_DELETE_CANCELLED,
            params = emptyMap(),
        )
    }
}
