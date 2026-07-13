package com.quare.bibleplanner.feature.themeselection.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ThemeSelectionUiEvent : UiEvent {
    data class OnThemeSelected(
        val theme: Theme,
    ) : ThemeSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.THEME_CHANGED,
            params = mapOf(AnalyticsParams.THEME to theme.name.lowercase()),
        )
    }

    data class OnContrastSelected(
        val contrastType: ContrastType,
    ) : ThemeSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CONTRAST_CHANGED,
            params = mapOf(AnalyticsParams.CONTRAST to contrastType.name.lowercase()),
        )
    }

    data object OnDismiss : ThemeSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.THEME_SELECTION_DISMISSED,
            params = emptyMap(),
        )
    }

    data object MaterialYouInfoClicked : ThemeSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.MATERIAL_YOU_INFO_CLICKED,
            params = emptyMap(),
        )
    }

    data class MaterialYouToggleClicked(
        val isNewValueOn: Boolean,
    ) : ThemeSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DYNAMIC_COLORS_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_ENABLED to isNewValueOn,
                AnalyticsParams.SOURCE to DYNAMIC_COLORS_SOURCE,
            ),
        )
    }

    data class SyncToggleClicked(
        val isNewValueOn: Boolean,
    ) : ThemeSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.SETTING_SYNC_TOGGLED,
            params = mapOf(
                AnalyticsParams.SETTING to SYNC_SETTING,
                AnalyticsParams.IS_ENABLED to isNewValueOn,
            ),
        )
    }

    data object SyncToggleBlockedClicked : ThemeSelectionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.SYNC_TOGGLE_BLOCKED_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to DYNAMIC_COLORS_SOURCE),
        )
    }

    companion object {
        private const val DYNAMIC_COLORS_SOURCE = "theme_selection"
        private const val SYNC_SETTING = "theme"
    }
}
