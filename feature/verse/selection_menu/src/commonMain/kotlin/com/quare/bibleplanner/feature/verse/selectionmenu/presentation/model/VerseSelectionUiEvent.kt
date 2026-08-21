package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface VerseSelectionUiEvent : UiEvent {
    data object OnClearSelectionClick : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.VERSE_SELECTION_CLEARED,
            params = emptyMap(),
        )
    }

    data class OnHighlightColorClick(
        val color: HighlightColor,
    ) : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.VERSE_HIGHLIGHT_APPLIED,
                AnalyticsEventNames.VERSE_HIGHLIGHT_REMOVED,
            ),
        )
    }

    data object OnCustomColorPickerOpen : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.HIGHLIGHT_COLOR_PICKER_OPENED,
                AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_LOCKED_CLICKED,
            ),
        )
    }

    data class OnCustomColorChange(
        val hue: Int,
        val lightness: Int,
    ) : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnCustomColorApplyClick : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_CREATED,
        )
    }

    data object OnCustomColorCancelClick : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.HIGHLIGHT_COLOR_PICKER_DISMISSED,
            params = emptyMap(),
        )
    }

    data class OnCustomColorLongClick(
        val color: HighlightColor.Custom,
    ) : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_DELETE_OPENED,
            params = emptyMap(),
        )
    }

    data object OnToggleSavedClick : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SAVED_TOGGLED,
        )
    }

    data object OnNoteClick : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_NOTE_OPENED,
        )
    }

    data object OnCopyClick : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSES_COPIED,
        )
    }

    data object OnShareClick : VerseSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_OPENED,
        )
    }
}
