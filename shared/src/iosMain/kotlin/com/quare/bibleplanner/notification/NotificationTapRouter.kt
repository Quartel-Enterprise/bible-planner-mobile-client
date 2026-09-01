package com.quare.bibleplanner.notification

import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import org.koin.mp.KoinPlatform

object NotificationTapRouter {
    private var navigator: Navigator? = null

    fun setNavigator(navigator: Navigator) {
        this.navigator = navigator
    }

    fun routeToBibleVersions() {
        val navigator = navigator ?: return
        val trackEvent = KoinPlatform.getKoin().get<TrackEvent>()
        trackEvent(
            name = AnalyticsEventNames.NOTIFICATION_OPENED,
            params = mapOf(AnalyticsParams.TYPE to NotificationAnalyticsType.VERSION_DOWNLOAD_COMPLETE),
        )
        navigator.navigate(BibleVersionSelectorRoute)
    }
}
