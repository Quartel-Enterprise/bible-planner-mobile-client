package com.quare.bibleplanner.notification

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import org.koin.mp.KoinPlatform

object NotificationTapRouter {
    private var navigationEventBus: NavigationEventBus? = null

    fun setNavigationEventBus(bus: NavigationEventBus) {
        navigationEventBus = bus
    }

    fun routeToBibleVersions() {
        val bus = navigationEventBus ?: return
        val trackEvent = KoinPlatform.getKoin().get<TrackEvent>()
        trackEvent(
            name = AnalyticsEventNames.NOTIFICATION_OPENED,
            params = mapOf(AnalyticsParams.TYPE to NotificationAnalyticsType.VERSION_DOWNLOAD_COMPLETE),
        )
        bus.send(BibleVersionSelectorRoute)
    }
}
