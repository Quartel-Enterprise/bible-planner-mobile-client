package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

sealed interface MainNavRouteDestination : NavRoute {
    @Serializable
    data object Plans : MainNavRouteDestination

    @Serializable
    data object Profile : MainNavRouteDestination

    @Serializable
    data object Books : MainNavRouteDestination
}
