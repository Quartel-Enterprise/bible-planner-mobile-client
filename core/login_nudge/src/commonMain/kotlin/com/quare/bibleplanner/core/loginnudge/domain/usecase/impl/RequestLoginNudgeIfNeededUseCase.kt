package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.loginnudge.domain.usecase.ShouldShowLoginNudge
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

internal class RequestLoginNudgeIfNeededUseCase(
    private val shouldShowLoginNudge: ShouldShowLoginNudge,
    private val navigationEventBus: NavigationEventBus,
    private val trackEvent: TrackEvent,
) : RequestLoginNudgeIfNeeded {
    override suspend fun invoke() {
        if (shouldShowLoginNudge()) {
            trackEvent(
                name = AnalyticsEventNames.LOGIN_NUDGE_SHOWN,
                params = emptyMap(),
            )
            navigationEventBus.send(LoginSyncNudgeNavRoute)
        }
    }
}
