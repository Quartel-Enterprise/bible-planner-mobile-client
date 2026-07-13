package com.quare.bibleplanner.feature.applanguage.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface AppLanguageUiEvent : UiEvent {
    data class OnLanguageSelected(
        val language: Language,
    ) : AppLanguageUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LANGUAGE_CHANGED,
            params = mapOf(AnalyticsParams.LANGUAGE to language.toAnalyticsLanguage()),
        )
    }

    data object OnDismiss : AppLanguageUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.APP_LANGUAGE_DISMISSED,
            params = emptyMap(),
        )
    }

    data class SyncToggleClicked(
        val isNewValueOn: Boolean,
    ) : AppLanguageUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.SETTING_SYNC_TOGGLED,
            params = mapOf(
                AnalyticsParams.SETTING to SYNC_SETTING,
                AnalyticsParams.IS_ENABLED to isNewValueOn,
            ),
        )

        private companion object {
            const val SYNC_SETTING = "language"
        }
    }

    data object SyncToggleBlockedClicked : AppLanguageUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.SYNC_TOGGLE_BLOCKED_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to SYNC_TOGGLE_SOURCE),
        )
    }

    private companion object {
        const val SYNC_TOGGLE_SOURCE = "app_language"
    }
}
