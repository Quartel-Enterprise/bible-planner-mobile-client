package com.quare.bibleplanner.android.feature.main

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination

internal data class MainTabBackStep(
    val tab: MainNavRouteDestination,
    val poppedRoute: NavKey?,
)
