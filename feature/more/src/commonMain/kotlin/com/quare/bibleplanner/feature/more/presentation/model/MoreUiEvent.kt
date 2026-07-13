package com.quare.bibleplanner.feature.more.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface MoreUiEvent : UiEvent {
    data class OnItemClick(
        val type: MoreOptionItemType,
    ) : MoreUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.MORE_OPTION_CLICKED,
            params = mapOf(AnalyticsParams.OPTION to type.toAnalyticsOption()),
        )
    }

    data object OnProCardClick : MoreUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PRO_CARD_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnAccountCardClick : MoreUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ACCOUNT_CARD_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnLoginClick : MoreUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGIN_ROW_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnLogoutClick : MoreUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGOUT_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to "more_menu"),
        )
    }
}
