package com.quare.bibleplanner.feature.verse.share.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.utils.presentation.UiEvent
import io.github.vinceglb.filekit.PlatformFile

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

    data object OnCustomColorClick : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.VERSE_SHARE_COLOR_PICKER_TOGGLED,
                AnalyticsEventNames.VERSE_SHARE_STYLE_CHANGED,
                AnalyticsEventNames.VERSE_SHARE_PRO_OPTION_LOCKED_CLICKED,
            ),
        )
    }

    // Why: fires on every slider step, so tracking it would flood the stream; the opened picker is tracked.
    data class OnCustomColorChange(
        val hue: Int,
        val lightness: Int,
    ) : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnPhotoBackgroundClick : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.VERSE_SHARE_PHOTO_PICKER_OPENED,
                AnalyticsEventNames.VERSE_SHARE_PRO_OPTION_LOCKED_CLICKED,
            ),
        )
    }

    data class OnPhotoPicked(
        val file: PlatformFile?,
    ) : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_STYLE_CHANGED,
        )
    }

    data object OnStreakToggleClick : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_STREAK_TOGGLED,
        )
    }

    data object OnWatermarkToggleClick : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.VERSE_SHARE_WATERMARK_TOGGLED,
                AnalyticsEventNames.VERSE_SHARE_PRO_OPTION_LOCKED_CLICKED,
            ),
        )
    }

    data class OnDownloadImageReady(
        val imageBytes: ByteArray,
    ) : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.VERSE_SHARE_IMAGE_DOWNLOADED,
        )

        override fun equals(other: Any?): Boolean =
            this === other || (other is OnDownloadImageReady && imageBytes.contentEquals(other.imageBytes))

        override fun hashCode(): Int = imageBytes.contentHashCode()
    }

    data object OnDismiss : ShareVerseUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.VERSE_SHARE_DISMISSED,
            params = emptyMap(),
        )
    }
}
