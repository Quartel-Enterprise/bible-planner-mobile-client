package com.quare.bibleplanner.feature.studyunlock.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface StudyUnlockUiEvent : UiEvent {
    data object OnSubscribeClick : StudyUnlockUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.UNLOCK_SUBSCRIBE_CLICKED,
        )
    }

    data object OnWatchVideoClick : StudyUnlockUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.REWARDED_AD_STARTED,
                AnalyticsEventNames.REWARDED_AD_EARNED,
                AnalyticsEventNames.REWARDED_AD_DISMISSED,
                AnalyticsEventNames.REWARDED_AD_FAILED,
            ),
        )
    }

    data object OnDismiss : StudyUnlockUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }
}
