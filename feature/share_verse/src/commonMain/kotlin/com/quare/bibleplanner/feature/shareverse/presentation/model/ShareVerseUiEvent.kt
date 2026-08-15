package com.quare.bibleplanner.feature.shareverse.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ShareVerseUiEvent : UiEvent {
    data object OnShareAsTextClick : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARED,
        )
    }

    data object OnShareAsImageClick : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_IMAGE_OPENED,
        )
    }

    data class OnBackgroundClick(
        val background: ShareCardBackground,
    ) : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_STYLE_CHANGED,
        )
    }

    data class OnFontClick(
        val font: ShareCardFont,
    ) : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_STYLE_CHANGED,
        )
    }

    /** Carries the rendered card, since the bytes only exist after the UI has drawn it. */
    data class OnShareImageReady(
        val imageBytes: ByteArray,
    ) : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARED,
        )

        override fun equals(other: Any?): Boolean =
            this === other || (other is OnShareImageReady && imageBytes.contentEquals(other.imageBytes))

        override fun hashCode(): Int = imageBytes.contentHashCode()
    }

    data object OnDismiss : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.VERSE_SHARE_DISMISSED,
            params = emptyMap(),
        )
    }
}
