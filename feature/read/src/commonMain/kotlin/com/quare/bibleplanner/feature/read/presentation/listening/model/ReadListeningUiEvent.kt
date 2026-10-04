package com.quare.bibleplanner.feature.read.presentation.listening.model

import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ReadListeningUiEvent : UiEvent {
    data class OnListenClick(
        val chapter: ChapterLocationModel,
        val source: ListeningEntrySource,
    ) : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CHAPTER_LISTENING_ENTRY_CLICKED,
        )
    }

    data object OnMiniPlayerClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_PLAYER_OPENED,
            params = mapOf(AnalyticsParams.SOURCE to ListeningSurface.MINI_PLAYER.key),
        )
    }

    data object OnPlayPauseClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CHAPTER_LISTENING_CONTROL_CLICKED,
        )
    }

    data object OnNextVerseClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_CONTROL_CLICKED,
            params = mapOf(
                AnalyticsParams.CONTROL to ListeningControl.NEXT_VERSE.key,
                AnalyticsParams.SURFACE to ListeningSurface.MINI_PLAYER.key,
            ),
        )
    }

    data object OnCloseClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_STOPPED,
            params = mapOf(AnalyticsParams.SURFACE to ListeningSurface.MINI_PLAYER.key),
        )
    }

    data object OnVoiceSettingsClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_VOICE_SETTINGS_OPENED,
            params = mapOf(AnalyticsParams.SURFACE to ListeningSurface.MINI_PLAYER.key),
        )
    }

    data object OnUnlockNextClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_UNLOCK_CLICKED,
            params = mapOf(AnalyticsParams.SURFACE to ListeningSurface.MINI_PLAYER.key),
        )
    }

    data object OnFinishOfferDismissClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_FINISH_OFFER_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnBackToVerseClick : ReadListeningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_BACK_TO_VERSE_CLICKED,
            params = emptyMap(),
        )
    }
}
