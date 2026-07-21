package com.quare.bibleplanner.feature.profile.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface ProfileUiEvent : UiEvent {
    data class OnItemClick(
        val type: ProfileOptionItemType,
    ) : ProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_OPTION_CLICKED,
            params = mapOf(AnalyticsParams.OPTION to type.toAnalyticsOption()),
        )
    }

    data object OnProCardClick : ProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PRO_CARD_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnAccountCardClick : ProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ACCOUNT_CARD_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnEditProfileClick : ProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.EDIT_PROFILE_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnAvatarClick : ProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_AVATAR_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnLoginClick : ProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGIN_ROW_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnLogoutClick : ProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGOUT_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to "profile_menu"),
        )
    }
}
