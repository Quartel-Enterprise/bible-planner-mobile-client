package com.quare.bibleplanner.feature.loginsyncnudge.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface LoginSyncNudgeUiEvent : UiEvent {
    data object OnLoginClick : LoginSyncNudgeUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGIN_NUDGE_ACCEPTED,
            params = emptyMap(),
        )
    }

    data object OnNotNow : LoginSyncNudgeUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.LOGIN_NUDGE_DISABLED,
                AnalyticsEventNames.LOGIN_NUDGE_SNOOZED,
            ),
        )
    }

    data object OnDismiss : LoginSyncNudgeUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.LOGIN_NUDGE_DISABLED,
                AnalyticsEventNames.LOGIN_NUDGE_SNOOZED,
            ),
        )
    }

    data class OnDontShowAgainToggled(
        val isChecked: Boolean,
    ) : LoginSyncNudgeUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGIN_NUDGE_DONT_SHOW_AGAIN_TOGGLED,
            params = mapOf(AnalyticsParams.IS_ENABLED to isChecked),
        )
    }
}
