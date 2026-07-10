package com.quare.bibleplanner.core.provider.analytics.domain.mapper

import com.quare.bibleplanner.core.model.route.NavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.Destination

fun interface NavRouteToDestinationMapper {
    fun map(route: NavRoute): Destination?
}
