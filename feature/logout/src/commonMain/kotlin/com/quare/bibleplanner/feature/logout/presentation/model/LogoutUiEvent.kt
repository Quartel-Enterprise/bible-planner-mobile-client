package com.quare.bibleplanner.feature.logout.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface LogoutUiEvent : UiEvent {
    sealed interface ConfirmLogoutClick : LogoutUiEvent {
        val shouldFlushPending: Boolean

        data object OnConfirmLogout : ConfirmLogoutClick {
            override val shouldFlushPending: Boolean = true
            override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
                name = AnalyticsEventNames.LOGOUT_CONFIRMED,
                params = mapOf(AnalyticsParams.IS_FORCED to false),
            )
        }

        /** Sign out without flushing pending changes — chosen after a [LogoutUiState.PendingChangesError]. */
        data object OnForceLogout : ConfirmLogoutClick {
            override val shouldFlushPending: Boolean = false
            override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
                name = AnalyticsEventNames.LOGOUT_CONFIRMED,
                params = mapOf(AnalyticsParams.IS_FORCED to true),
            )
        }
    }

    data object OnCancel : LogoutUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGOUT_CANCELLED,
            params = emptyMap(),
        )
    }

    data object OnDismiss : LogoutUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGOUT_CANCELLED,
            params = emptyMap(),
        )
    }
}
