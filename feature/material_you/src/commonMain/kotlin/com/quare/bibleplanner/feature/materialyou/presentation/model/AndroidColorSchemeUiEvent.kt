package com.quare.bibleplanner.feature.materialyou.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface AndroidColorSchemeUiEvent : UiEvent {
    data class OnIsDynamicColorsEnabledChange(
        val isEnabled: Boolean,
    ) : AndroidColorSchemeUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DYNAMIC_COLORS_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_ENABLED to isEnabled,
                AnalyticsParams.SOURCE to DYNAMIC_COLORS_SOURCE,
            ),
        )
    }

    data object OnInformationDialogDismiss : AndroidColorSchemeUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.MATERIAL_YOU_INFO_DISMISSED,
            params = emptyMap(),
        )
    }

    data object BottomSheetGotItClick : AndroidColorSchemeUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.MATERIAL_YOU_GOT_IT_CLICKED,
            params = emptyMap(),
        )
    }

    companion object {
        private const val DYNAMIC_COLORS_SOURCE = "material_you"
    }
}
