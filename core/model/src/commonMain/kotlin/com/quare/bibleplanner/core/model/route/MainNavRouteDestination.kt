package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface MainNavRouteDestination : NavKey {
    @Serializable
    data object Plans : MainNavRouteDestination

    @Serializable
    data object More : MainNavRouteDestination

    @Serializable
    data object Books : MainNavRouteDestination
}
