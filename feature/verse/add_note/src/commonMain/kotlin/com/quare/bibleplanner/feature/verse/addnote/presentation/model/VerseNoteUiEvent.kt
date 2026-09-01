package com.quare.bibleplanner.feature.verse.addnote.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface VerseNoteUiEvent : UiEvent {
    /** Typing is not an action to attribute; only the explicit save is. */
    data class OnTextChange(
        val text: String,
    ) : VerseNoteUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnSaveClick : VerseNoteUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_NOTE_SAVED,
        )
    }

    data object OnDismiss : VerseNoteUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.VERSE_NOTE_DISMISSED,
            params = emptyMap(),
        )
    }
}
