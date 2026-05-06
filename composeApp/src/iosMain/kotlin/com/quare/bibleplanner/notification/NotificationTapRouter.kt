package com.quare.bibleplanner.notification

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute

object NotificationTapRouter {
    private var navigationEventBus: NavigationEventBus? = null

    fun setNavigationEventBus(bus: NavigationEventBus) {
        navigationEventBus = bus
    }

    fun routeToBibleVersions() {
        navigationEventBus?.send(BibleVersionSelectorRoute)
    }
}
