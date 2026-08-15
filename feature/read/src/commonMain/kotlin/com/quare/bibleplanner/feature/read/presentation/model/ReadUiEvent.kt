package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ReadUiEvent : UiEvent {
    data object OnArrowBackClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.READ_BACK_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnRetryClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.READ_RETRY_CLICKED,
            params = emptyMap(),
        )
    }

    /**
     * Carries the chapter because vertical reading keeps two chapters — and two read pills — on the
     * same screen.
     */
    data class ToggleReadStatus(
        val bookId: BookId,
        val chapterNumber: Int,
    ) : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CHAPTER_READ_TOGGLED,
        )
    }

    data object OnDownloadSelectedVersionClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_STARTED,
        )
    }

    data object ManageBibleVersions : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_MANAGE_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to "read_screen"),
        )
    }

    data class OnNavigationSuggestionClick(
        val suggestion: ReadNavigationSuggestionModel,
    ) : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READING_SUGGESTION_CLICKED,
        )
    }

    data class OnVerseClick(
        val bookId: BookId,
        val chapterNumber: Int,
        val verseNumber: Int,
    ) : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SELECTION_TOGGLED,
        )
    }

    data object OnClearSelectionClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.VERSE_SELECTION_CLEARED,
            params = emptyMap(),
        )
    }

    data class OnHighlightColorClick(
        val color: HighlightColor,
    ) : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.VERSE_HIGHLIGHT_APPLIED,
                AnalyticsEventNames.VERSE_HIGHLIGHT_REMOVED,
            ),
        )
    }

    data object OnCustomColorPickerOpen : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.HIGHLIGHT_COLOR_PICKER_OPENED,
            params = emptyMap(),
        )
    }

    data class OnCustomColorChange(
        val hue: Int,
        val lightness: Int,
    ) : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnCustomColorApplyClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_CREATED,
        )
    }

    data object OnCustomColorCancelClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.HIGHLIGHT_COLOR_PICKER_DISMISSED,
            params = emptyMap(),
        )
    }

    data class OnCustomColorLongClick(
        val color: HighlightColor.Custom,
    ) : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_DELETE_OPENED,
            params = emptyMap(),
        )
    }

    data object OnToggleSavedClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SAVED_TOGGLED,
        )
    }

    data object OnNoteClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_NOTE_OPENED,
        )
    }

    data object OnCopyClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSES_COPIED,
        )
    }

    data object OnShareClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_OPENED,
        )
    }

    data object OnAppearanceClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.READER_APPEARANCE_OPENED,
            params = emptyMap(),
        )
    }

    data object OnSidePanelToggleClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READ_SIDE_PANEL_TOGGLED,
        )
    }

    data object OnRulerDismissClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READER_FOCUS_AID_CHANGED,
        )
    }
}
