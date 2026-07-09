package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.quare.bibleplanner.core.model.route.DayNavRoute

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.day(
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
) {
    entry<DayNavRoute> { route ->
        DayRootContent(
            route = route,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = LocalNavAnimatedContentScope.current,
        )
    }
}
