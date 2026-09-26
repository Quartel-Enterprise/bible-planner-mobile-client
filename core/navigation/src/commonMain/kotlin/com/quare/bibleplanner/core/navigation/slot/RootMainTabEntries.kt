package com.quare.bibleplanner.core.navigation.slot

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.feature.books.presentation.booksScreen
import com.quare.bibleplanner.feature.main.presentation.MainTabEntries
import com.quare.bibleplanner.feature.profile.presentation.profile
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan

@OptIn(ExperimentalSharedTransitionApi::class)
internal class RootMainTabEntries(
    private val sharedTransitionScope: SharedTransitionScope,
) : MainTabEntries {
    override fun register(
        scope: EntryProviderScope<NavKey>,
        navigationBar: @Composable (Modifier) -> Unit,
        navigationRail: @Composable () -> Unit,
        animatedContentScope: AnimatedContentScope,
    ) {
        scope.readingPlan(
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
        scope.booksScreen(
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedContentScope,
        )
        scope.profile(
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
}
