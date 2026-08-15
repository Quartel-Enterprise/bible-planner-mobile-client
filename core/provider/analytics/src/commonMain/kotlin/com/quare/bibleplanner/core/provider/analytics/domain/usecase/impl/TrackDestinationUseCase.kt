package com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.NavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.mapper.NavRouteToDestinationMapper
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.DestinationType
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackDestination
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

class TrackDestinationUseCase(
    private val mapper: NavRouteToDestinationMapper,
    private val trackEvent: TrackEvent,
) : TrackDestination {
    override fun invoke(navKey: NavKey) {
        val destination = (navKey as? NavRoute)?.let(mapper::map) ?: return
        trackEvent(
            name = AnalyticsEventNames.SCREEN_VIEW,
            params = destination.params +
                (AnalyticsParams.SCREEN_NAME to destination.name) +
                (AnalyticsParams.SCREEN_CLASS to destination.type.toAnalyticsValue()),
        )
    }

    private fun DestinationType.toAnalyticsValue(): String = name.lowercase()
}
