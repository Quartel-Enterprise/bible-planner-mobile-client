package com.quare.bibleplanner.feature.main.presentation.navhost

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination

internal data class TabBackStep(
    val tab: MainNavRouteDestination,
    val poppedRoute: NavKey?,
)
