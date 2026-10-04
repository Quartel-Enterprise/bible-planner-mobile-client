package com.quare.bibleplanner.feature.read.presentation.listening.player

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningControl
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningSurface
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ChapterListeningPlayerUiEvent : UiEvent {
    data object OnPlayPauseClick : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CHAPTER_LISTENING_CONTROL_CLICKED,
        )
    }

    data object OnPreviousVerseClick : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = createControlAnalytics(ListeningControl.PREVIOUS_VERSE)
    }

    data object OnNextVerseClick : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = createControlAnalytics(ListeningControl.NEXT_VERSE)
    }

    data object OnPreviousChapterClick : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = createControlAnalytics(ListeningControl.PREVIOUS_CHAPTER)
    }

    data object OnNextChapterClick : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = createControlAnalytics(ListeningControl.NEXT_CHAPTER)
    }

    data class OnSeek(
        val verseIndex: Int,
    ) : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = createControlAnalytics(ListeningControl.SEEK)
    }

    data class OnSpeedClick(
        val speed: Float,
    ) : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_SPEED_CHANGED,
            params = mapOf(AnalyticsParams.SPEED to speed.toDouble()),
        )
    }

    data class OnVoiceClick(
        val voice: ListeningVoiceOptionUiModel,
    ) : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_VOICE_CHANGED,
            params = mapOf(AnalyticsParams.IS_ENHANCED to voice.isEnhanced),
        )
    }

    data class OnVoicePreviewClick(
        val voice: ListeningVoiceOptionUiModel,
    ) : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_VOICE_PREVIEWED,
            params = mapOf(AnalyticsParams.IS_ENHANCED to voice.isEnhanced),
        )
    }

    data object OnVoiceSettingsClick : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_VOICE_SETTINGS_OPENED,
            params = mapOf(AnalyticsParams.SURFACE to ListeningSurface.PLAYER.key),
        )
    }

    data class OnSleepTimerClick(
        val option: ListeningSleepTimerOption,
    ) : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_SLEEP_TIMER_SET,
            params = mapOf(AnalyticsParams.OPTION to option.key),
        )
    }

    data class OnAutoNextToggle(
        val isEnabled: Boolean,
    ) : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_AUTO_NEXT_TOGGLED,
            params = mapOf(AnalyticsParams.IS_ENABLED to isEnabled),
        )
    }

    data object OnUnlockClick : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_UNLOCK_CLICKED,
            params = mapOf(AnalyticsParams.SURFACE to ListeningSurface.PLAYER.key),
        )
    }

    data object OnDismiss : ChapterListeningPlayerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_LISTENING_PLAYER_CLOSED,
            params = emptyMap(),
        )
    }
}

private fun createControlAnalytics(control: ListeningControl): EventAnalytics = EventAnalytics.Track.Automatic(
    name = AnalyticsEventNames.CHAPTER_LISTENING_CONTROL_CLICKED,
    params = mapOf(
        AnalyticsParams.CONTROL to control.key,
        AnalyticsParams.SURFACE to ListeningSurface.PLAYER.key,
    ),
)
