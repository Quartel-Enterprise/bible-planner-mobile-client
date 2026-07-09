package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface BottomNavRoute : NavKey {
    @Serializable
    data object Plans : BottomNavRoute

    @Serializable
    data object More : BottomNavRoute

    @Serializable
    data object Books : BottomNavRoute
}
