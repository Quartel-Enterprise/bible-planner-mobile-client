package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

sealed interface BottomNavRoute {
    @Serializable
    data object Plans : BottomNavRoute

    @Serializable
    data object More : BottomNavRoute
}
