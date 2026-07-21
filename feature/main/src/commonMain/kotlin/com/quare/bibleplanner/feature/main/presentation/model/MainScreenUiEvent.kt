package com.quare.bibleplanner.feature.main.presentation.model

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface MainScreenUiEvent : UiEvent {
    data class BottomNavItemClicked(
        val route: NavKey,
    ) : MainScreenUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOTTOM_TAB_CLICKED,
            params = mapOf(AnalyticsParams.TAB to route.toTabName()),
        )
    }
}

private fun NavKey.toTabName(): String = when (this) {
    MainNavRouteDestination.Plans -> "plans"
    MainNavRouteDestination.Books -> "books"
    MainNavRouteDestination.Profile -> "profile"
    else -> "unknown"
}
