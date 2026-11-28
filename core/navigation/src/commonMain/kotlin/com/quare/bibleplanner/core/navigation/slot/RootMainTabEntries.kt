package com.quare.bibleplanner.core.navigation.slot

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.navigation.NativeTabContentProvider
import com.quare.bibleplanner.feature.books.presentation.booksScreen
import com.quare.bibleplanner.feature.main.presentation.MainTabEntries
import com.quare.bibleplanner.feature.profile.presentation.profile
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan
import com.quare.bibleplanner.ui.utils.MainTabScaffold

internal class RootMainTabEntries(
    private val sharedTransitionScope: SharedTransitionScope,
    private val nativeTabContent: NativeTabContentProvider? = null,
) : MainTabEntries {
    override fun register(
        scope: EntryProviderScope<NavKey>,
        navigationBar: @Composable (Modifier) -> Unit,
        navigationRail: @Composable () -> Unit,
        animatedContentScope: AnimatedContentScope,
    ) {
        scope.tab(
            tab = MainNavRouteDestination.Plans,
            navigationBar = navigationBar,
            navigationRail = navigationRail,
        ) {
            readingPlan(
                navigationBar = navigationBar,
                navigationRail = navigationRail,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }
        scope.tab(
            tab = MainNavRouteDestination.Books,
            navigationBar = navigationBar,
            navigationRail = navigationRail,
        ) {
            booksScreen(
                navigationBar = navigationBar,
                navigationRail = navigationRail,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedContentScope,
            )
        }
        scope.tab(
            tab = MainNavRouteDestination.Profile,
            navigationBar = navigationBar,
            navigationRail = navigationRail,
        ) {
            profile(
                navigationBar = navigationBar,
                navigationRail = navigationRail,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }
    }

    // Why: a tab with a native body keeps the shared scaffold, so the shared tab bar stays around it.
    private fun EntryProviderScope<NavKey>.tab(
        tab: MainNavRouteDestination,
        navigationBar: @Composable (Modifier) -> Unit,
        navigationRail: @Composable () -> Unit,
        registerShared: EntryProviderScope<NavKey>.() -> Unit,
    ) {
        val nativeContent = nativeTabContent?.contentFor(tab)
        if (nativeContent == null) {
            registerShared()
            return
        }
        entry(key = tab) {
            MainTabScaffold(
                navigationBar = navigationBar,
                navigationRail = navigationRail,
                content = nativeContent,
            )
        }
    }
}
