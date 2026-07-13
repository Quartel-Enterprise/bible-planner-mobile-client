package com.quare.bibleplanner.feature.loginwarning.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface LoginWarningUiEvent : UiEvent {
    data object OnLoginClick : LoginWarningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.LOGIN_WARNING_ACCEPTED,
        )
    }

    data object OnDismiss : LoginWarningUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.LOGIN_WARNING_DISMISSED,
        )
    }
}
