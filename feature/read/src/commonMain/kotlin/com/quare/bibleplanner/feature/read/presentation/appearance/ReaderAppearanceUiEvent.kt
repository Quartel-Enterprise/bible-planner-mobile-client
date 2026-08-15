package com.quare.bibleplanner.feature.read.presentation.appearance

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ReaderAppearanceUiEvent : UiEvent {
    /** Fired continuously while the slider is dragged, so only the committed value is tracked. */
    data class OnFontSizeChange(
        val fontSizeSp: Float,
    ) : ReaderAppearanceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnFontSizeChangeFinished(
        val fontSizeSp: Float,
    ) : ReaderAppearanceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READER_FONT_SIZE_CHANGED,
        )
    }

    data class OnFontClick(
        val font: ReaderFont,
    ) : ReaderAppearanceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READER_FONT_CHANGED,
        )
    }

    data class OnFontMenuToggle(
        val isExpanded: Boolean,
    ) : ReaderAppearanceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READER_FONT_MENU_TOGGLED,
        )
    }

    data class OnFocusAidChange(
        val focusAid: ReaderFocusAid,
    ) : ReaderAppearanceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READER_FOCUS_AID_CHANGED,
        )
    }

    data class OnVerticalReadingChange(
        val isEnabled: Boolean,
    ) : ReaderAppearanceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READER_VERTICAL_READING_TOGGLED,
        )
    }

    data object OnDismiss : ReaderAppearanceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.READER_APPEARANCE_DISMISSED,
            params = emptyMap(),
        )
    }
}
